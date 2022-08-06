const mongoose = require("mongoose");
const { FoodoRestaurantSchema } = require("./FoodoRestaurant");

const FoodoListSchema = mongoose.Schema({
  name: {
    type: String,
    required: true,
  },
  restaurants: {
    type: [FoodoRestaurantSchema],
  },
  users: {
    type: [String],
  },
});


const FoodoListModel = mongoose.model("FoodoList", FoodoListSchema);

module.exports = {
  FoodoListSchema,
  FoodoListModel,
};
