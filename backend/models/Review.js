const mongoose = require("mongoose");

const ReviewSchema = mongoose.Schema({
  google_place_id: {
    type: String,
    required: true,
  },
  user_name: {
    type: String,
    required: true,
  },
  review: {
    type: String,
    required: true,
  },
  rating: {
    type: Number,
    required: true,
  },
  user_id: {
    type: String,
    required: true,
  }
});

module.exports = mongoose.model("Review", ReviewSchema);
