const Review = require("../models/Review");

async function getReviews(google_place_id) {
  return Review.find({
    google_place_id: google_place_id,
  })
    .then((reviews) => {
      return reviews;
    })
    .catch((err) => {
      console.log(err);
    });
}

module.exports = getReviews;
