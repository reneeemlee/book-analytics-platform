package com.reneeemlee.book_analytics.service;

import com.reneeemlee.book_analytics.dto.NYTBookResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
                .flatMap(this::fetchAndAttachBookCovers);
    }

    private Mono<NYTBookResponse> fetchAndAttachBookCovers(NYTBookResponse response) {
        if (response.getResults() == null || response.getResults().getBooks() == null) {
            return Mono.just(response);
        }

        return Flux.fromIterable(response.getResults().getBooks())
                .flatMap(book -> fetchCoverForBook(book)
                        .doOnNext(book::setBookImage)
                        .thenReturn(book))
                .then(Mono.just(response));
    }

    private Mono<String> fetchCoverForBook(NYTBookResponse.BookDetail book) {
        String isbn = book.getIsbn13();

        // 1. Try Google Books by ISBN
        return queryGoogleBooks("isbn:" + (isbn != null ? isbn : ""))
                .flatMap(url -> {
                    if (!url.isBlank()) {
                        return Mono.just(url);
                    }
                    // 2. Try Google Books by Title + Author
                    String cleanTitle = book.getTitle() != null ? book.getTitle() : "";
                    String cleanAuthor = book.getAuthor() != null ? book.getAuthor() : "";
                    return queryGoogleBooks(cleanTitle + " " + cleanAuthor);
                })
                .flatMap(url -> {
                    if (!url.isBlank()) {
                        return Mono.just(url);
                    }
                    // 3. Guaranteed Fallback: Open Library CDN direct image URL
                    if (isbn != null && !isbn.isBlank()) {
                        return Mono.just("https://covers.openlibrary.org/b/isbn/" + isbn + "-L.jpg");
                    }
                    return Mono.just("");
                });
    }

    private Mono<String> queryGoogleBooks(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isBlank()) {
            return Mono.just("");
        }

        String encodedQuery = URLEncoder.encode(searchTerm.trim(), StandardCharsets.UTF_8);

        return this.googleBooksWebClient.get()
                .uri("/volumes?q=" + encodedQuery + "&maxResults=1")
                .header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)")
                .retrieve()
                .bodyToMono(Map.class)
                .map(this::extractThumbnailFromGoogleMap)
                .doOnError(err -> System.err.println("Google Books API fetch notice: " + err.getMessage()))
                .onErrorReturn("");
    }

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
                        if (thumbnail != null && !thumbnail.isBlank()) {
                            return thumbnail.replace("http://", "https://");
                        }
                    }
                }
            }
        } catch (Exception e) {
            // Silently fall through
        }
        return "";
    }
}