const mongoose = require("mongoose");
const {
  FoodoRestaurantSchema,
  FoodoRestaurantModel,
} = require("./FoodoRestaurant");
const { UserAccountSchema, UserAccountModel } = require("./UserAccount");

const FoodoListSchema = mongoose.Schema({
  name: {
    type: String,
    required: true,
  },
  restaurants: {
    type: [FoodoRestaurantSchema],
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
