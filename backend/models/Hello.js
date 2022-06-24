const mongoose = require("mongoose");

const HelloSchema = mongoose.Schema({
  title: {
    type: String,
    required: true,
  },
  message: {
    type: String,
  },
});

module.exports = mongoose.model("Hello", HelloSchema);
