const mongoose = require("mongoose");
const FoodoRestaurant = require("./FoodoRestaurant");

const FoodoListSchema = mongoose.Schema({
  name: {
    type: String,
    required: true,
  },
  restaurants: {
    type: [FoodoRestaurant],
  },
  users: {
    type: [String], // email addresses, should we create a UserAccount schema instead?
  },
});

module.exports = mongoose.model("FoodoList", FoodoListSchema);
