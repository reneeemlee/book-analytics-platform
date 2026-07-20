package com.reneeemlee.book_analytics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.reneeemlee.book_analytics")
public class BookAnalyticsApplication {

    public static void main(String[] args) {
        SpringApplication.run(BookAnalyticsApplication.class, args);
    }
}