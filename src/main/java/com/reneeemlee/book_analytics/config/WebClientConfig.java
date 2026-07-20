package com.reneeemlee.book_analytics.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient nytWebClient() { // Changed from webClient to nytWebClient
        return WebClient.builder()
                .baseUrl("https://api.nytimes.com")
                .build();
    }

    @Bean
    public WebClient googleBooksWebClient() {
        return WebClient.builder()
                .baseUrl("https://www.googleapis.com/books/v1")
                .build();
    }
}