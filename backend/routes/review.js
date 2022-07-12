const express = require("express");
const router = express.Router();
const Review = require("../models/Review");
const { getMessaging } = require("firebase-admin/messaging");
const { app } = require("../utils/firebase");

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
    console.log("Here");
    const review = new Review({ ...req.body });
    // Save this review to database
    const data = await review.save();
    // Send a message to devices subscribed to the provided topic.
    // Is it even sending to a topic
    const message = {
      data: { ...req.body },
      topic: req.body.google_place_id,
    };
    getMessaging(app)
      .send(message)
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

// Subscribe to snapshots to the review collection
// Takes GooglePlaceID to subscribe to as query
router.get("/subSnap", async (req, res) => {
  try {
    Review.watch().on("change", (data) => {
      if (data.fullDocument.google_place_id === req.query.google_place_id) {
        res.writeHead(200, {
          "Content-Type": "text/event-stream",
          "Cache-Control": "no-cache",
          "Access-Control-Allow-Origin": "*",
        });
        res.write({ review: data.fullDocument, change: data.operationType });
      }
    });
  } catch (error) {
    res.json(error);
  }
});

module.exports = router;
