package com.reneeemlee.book_analytics.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class NYTBookResponse {
    private String status;
    
    @JsonProperty("num_results")
    private int numResults; 
    
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
        private String isbn13;

        @JsonProperty("bookImage")
        private String bookImage;
    }
}