const express = require("express");
const router = express.Router();
const Review = require("../models/Review");
const { getMessaging } = require("firebase-admin/messaging");

// Get all reviews of a restaurant
router.get("/getReviews", async (req, res) => {
  const reviews = await Review.find({
    google_place_id: req.query.google_place_id,
  });
  res.json(reviews);
});

// Post a new review
router.post("/addReview", async (req, res) => {
  try {
    const review = new Review({
      google_place_id: req.body.google_place_id,
      user_name: req.body.user_name,
      review: req.body.review,
      rating: req.body.rating,
    });

    // Save this review to database
    const data = await review.save();
    // Send a message to devices subscribed to the provided topic.
    getMessaging()
      .send(review)
      .then((response) => {
        // Response is a message ID string.
        console.log("Successfully sent message:", response);
      })
      .catch((error) => {
        console.log("Error sending message:", error);
      });
    res.json(data);
  } catch (err) {
    res.json(err);
  }
});

// Delete a review
router.delete("/deleteReview", async (req, res) => {
  try {
    const result = await Review.deleteOne({ _id: req.body.id });
    res.json(result);
  } catch (err) {
    res.json(err);
  }
});

module.exports = router;
