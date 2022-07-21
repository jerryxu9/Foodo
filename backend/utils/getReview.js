const Review = require("../models/Review");

async function getReviews(id) {
  return Review.find({
    google_place_id: id,
  })
    .then((reviews) => {
      return reviews;
    })
    .catch((err) => {
      console.log(err);
    });
}

module.exports = getReviews;
