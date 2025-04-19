package com.example.quickvendadmin;

public class Review {
    private String reviewerName;
    private String reviewText;
    private long timestamp;
    private String formattedTimestamp;
    private int rating;

    public Review() {} // Required for Firebase

    public String getReviewerName() {
        return reviewerName;
    }

    public String getReviewText() {
        return reviewText;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getFormattedTimestamp() {
        return formattedTimestamp;
    }

    public int getRating() {
        return rating;
    }
}
