package com.example.foodo.objects;

public class ReviewCard {
    private String reviewerName;
    private String reviewText;
    private String reviewRating;
    private String reviewId;

    public ReviewCard(String reviewName, String reviewText, String reviewRating, String reviewId){
        this.reviewerName = reviewName;
        this.reviewText = reviewText;
        this.reviewRating = reviewRating;
        this.reviewId = reviewId;
    }
    public String getReviewId(){
        return reviewId;
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
