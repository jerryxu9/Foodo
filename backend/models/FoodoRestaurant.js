const mongoose = require("mongoose");

const FoodoRestaurantSchema = mongoose.Schema({
  place_id: {
    type: String,
    required: true,
  },
  name: {
    type: String,
    required: true,
  },
  isVisited: {
    type: Boolean,
    required: true,
  },
  lat: {
    type: Number,
    required: true,
  },
  lng: {
    type: Number,
    required: true,
  },
});

const FoodoRestaurantModel = mongoose.model(
  "FoodoRestaurant",
  FoodoRestaurantSchema
);

module.exports = {
  FoodoRestaurantSchema,
  FoodoRestaurantModel,
};
