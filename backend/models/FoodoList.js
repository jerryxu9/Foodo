const mongoose = require("mongoose");
const { FoodoRestaurantSchema } = require("./FoodoRestaurant");
const { UserAccountSchema } = require("./UserAccount");

const FoodoListSchema = mongoose.Schema({
  name: {
    type: String,
    required: true,
  },
  restaurants: {
    type: [FoodoRestaurantSchema],
    // type: {
    //   place_id: String,
    //   name: String,
    //   isVisited: Boolean,
    // },
  },
  users: {
    type: [UserAccountSchema], // should we user a UserAccount schema or just email?
  },
});

const FoodoListModel = mongoose.model("FoodoList", FoodoListSchema);

module.exports = {
  FoodoListSchema,
  FoodoListModel,
};
