package com.example.foodo.objects;

public class ReviewCard {
    private String reviewerName;
    private String reviewText;
    private String reviewRating;

    public ReviewCard(String reviewName, String reviewText, String reviewRating){
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
