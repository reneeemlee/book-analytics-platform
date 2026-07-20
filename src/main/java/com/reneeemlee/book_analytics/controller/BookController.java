package com.reneeemlee.book_analytics.controller;

import com.reneeemlee.book_analytics.dto.BestsellerListType;
import com.reneeemlee.book_analytics.dto.NYTBookResponse;
import com.reneeemlee.book_analytics.service.BookService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping("/bestsellers/{listType}")
    public Mono<NYTBookResponse> getBestsellers(@PathVariable BestsellerListType listType) {
        // Automatically extracts the correct API name from your selected menu item
        return bookService.getBestsellers(listType.getApiName());
    }

    @GetMapping("/lists")
    public java.util.List<java.util.Map<String, String>> getAvailableLists() {
        return java.util.Arrays.stream(BestsellerListType.values())
                .map(listType -> java.util.Map.of(
                        "id", listType.name(),
                        "name", listType.getDisplayName(),
                        "apiName", listType.getApiName()
                ))
                .collect(java.util.stream.Collectors.toList());
    }
}