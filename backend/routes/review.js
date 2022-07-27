const express = require("express");
const router = express.Router();
const Review = require("../models/Review");
const { getMessaging } = require("firebase-admin/messaging");

// Post a new review
router.post("/addReview", async (req, res) => {
  const review = new Review({
    google_place_id: req.body?.google_place_id,
    user_name: req.body?.user_name,
    review: req.body?.review,
    rating: req.body?.rating,
  });
  console.log(review);
  // Save this review to database
  const data = await review.save();

  const message = {
    data: {
      action: "add",
      google_place_id: review.google_place_id,
      user_name: review.user_name,
      review: review.review,
      rating: review.rating.toString(),
      id: data._id.toString()
    },
    topic: review.google_place_id
  }
  console.log(message)

  // Send a message to devices subscribed to the provided topic.
  getMessaging()
    .send(message)
    .then((response) => {
      // Response is a message ID string.
      console.log("Successfully sent message:", response);
    })
    .catch((error) => {
      console.log("Error sending message:", error);
    });

  res.json(data);
});

// Delete a review
router.delete("/deleteReview", async (req, res) => {
  Review.deleteOne({ _id: req.body?.id })
    .then(async (result) => {
      console.log(result);
      const message = {
        data: {
          action: "delete",
          id: req.body?.id,
          position: req.body?.position
        },
        topic: req.body?.google_place_id
      }

      console.log(message);
      getMessaging()
      .send(message)
      .then((response) => {
        // Response is a message ID string.
        console.log("Successfully sent request to delete:", response);
      })
      .catch((error) => {
        console.log("Error sending request to delete:", error);
      });

      res.json(result);
    })
    .catch((err) => {
      res.json(err);
    });
});

module.exports = router;
