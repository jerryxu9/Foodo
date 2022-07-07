const express = require("express");
const router = express.Router();
const Review = require("../models/Review");

// Get all reviews of a restaurant
router.get("/getReviews", async (req, res) => {
  try {
    const reviews = await Review.find({
      google_place_id: req.query.google_place_id,
    });
    res.json(reviews);
  } catch (err) {
    res.json(err);
  }
});

// Post a new review
router.post("/addReview", async (req, res) => {
  try {
    const review = new Review({ ...req.body });
    // Save this review to database
    const data = await review.save();
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
