package com.reneeemlee.book_analytics.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class NYTBookResponse {
    private String status;
    private int num_result; 
    private ListResult results;

    @Data
    public static class ListResult {
        @JsonProperty("list_name")
        private String listName;

        @JsonProperty("bestsellers_date")
        private String bestsellersDate;

        private List<BookDetail> books;
    }

    @Data
    public static class BookDetail {
        private int rank;
        private String title;
        private String author;
        private String description;
        private String publisher;

        @JsonProperty("primary_isbn13")
        private String primaryIsbn13;

        private String bookImage;

        // Convenient helper getter for frontend & service
        public String getIsbn13() {
            return primaryIsbn13;
        }

        public void setIsbn13(String isbn13) {
            this.primaryIsbn13 = isbn13;
        }
    }
}