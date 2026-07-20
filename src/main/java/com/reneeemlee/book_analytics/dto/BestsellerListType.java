package com.reneeemlee.book_analytics.dto;

public enum BestsellerListType {
    HARDCOVER_FICTION("Hardcover Fiction", "hardcover-fiction"),
    HARDCOVER_NONFICTION("Hardcover Nonfiction", "hardcover-nonfiction"),
    COMBINED_NONFICTION("Combined Print & E-Book Nonfiction", "combined-print-and-e-book-nonfiction"),
    BUSINESS_BOOKS("Business Books", "business-books");

    private final String displayName;
    private final String apiName;

    BestsellerListType(String displayName, String apiName) {
        this.displayName = displayName;
        this.apiName = apiName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getApiName() {
        return apiName;
    }
}
