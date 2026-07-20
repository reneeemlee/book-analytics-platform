package com.reneeemlee.book_analytics.service;

import com.reneeemlee.book_analytics.dto.NYTBookResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
public class BookService {

    private final WebClient nytWebClient;
    private final WebClient googleBooksWebClient;

    @Value("${nyt.api.key}")
    private String apiKey;

    public BookService(
            @Qualifier("nytWebClient") WebClient nytWebClient,
            @Qualifier("googleBooksWebClient") WebClient googleBooksWebClient) {
        this.nytWebClient = nytWebClient;
        this.googleBooksWebClient = googleBooksWebClient;
    }

    public Mono<NYTBookResponse> getBestsellers(String listName) {
        return this.nytWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/svc/books/v3/lists/current/{listName}.json")
                        .queryParam("api-key", apiKey)
                        .build(listName))
                .retrieve()
                .bodyToMono(NYTBookResponse.class)
                .flatMap(this::fetchAndAttachBookCovers); // Intercept the response to stitch Google data
    }

    // Iterates through every book concurrently to look up its cover
    private Mono<NYTBookResponse> fetchAndAttachBookCovers(NYTBookResponse response) {
        if (response.getResults() == null || response.getResults().getBooks() == null) {
            return Mono.just(response);
        }

        return Flux.fromIterable(response.getResults().getBooks())
                .flatMap(book -> fetchBookCoverUrl(book.getIsbn13())
                        .doOnNext(book::setBookImage)
                        .thenReturn(book))
                .then(Mono.just(response));
    }

    // Calls Google Books API for a specific ISBN and safely parses out the thumbnail string
    private Mono<String> fetchBookCoverUrl(String isbn) {
        if (isbn == null || isbn.isBlank()) {
            return Mono.just("");
        }

        return this.googleBooksWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/volumes")
                        .queryParam("q", "isbn:" + isbn)
                        .build())
                .header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)") // Helps prevent Google throttling
                .retrieve()
                .bodyToMono(Map.class)
                .map(this::extractThumbnailFromGoogleMap)
                .onErrorReturn(""); // Fail silently if cover isn't found
    }

    // Safe parsing helper to navigate Google's deeply nested JSON map structure
    @SuppressWarnings("unchecked")
    private String extractThumbnailFromGoogleMap(Map rawMap) {
        try {
            var items = (java.util.List<Map>) rawMap.get("items");
            if (items != null && !items.isEmpty()) {
                var volumeInfo = (Map) items.get(0).get("volumeInfo");
                if (volumeInfo != null) {
                    var imageLinks = (Map) volumeInfo.get("imageLinks");
                    if (imageLinks != null) {
                        String thumbnail = (String) imageLinks.get("thumbnail");
                        if (thumbnail != null) {
                            // Fixes browser mixed-content blocks by forcing secure links
                            return thumbnail.replace("http://", "https://");
                        }
                    }
                }
            }
        } catch (Exception e) {
            // Log or ignore extraction problems
        }
        return "";
    }
}