package com.example.foodo.objects;

public class ReviewCard {
    private final String reviewerName;
    private final String reviewText;
    private final String reviewRating;

    public ReviewCard(String reviewName, String reviewText, String reviewRating) {
        this.reviewerName = reviewName;
        this.reviewText = reviewText;
        this.reviewRating = reviewRating;
    }

    public String getReviewerName() {
        return reviewerName;
    }

    public String getReviewRating() {
        return reviewRating;
    }

    public String getReviewText() {
        return reviewText;
    }
}
