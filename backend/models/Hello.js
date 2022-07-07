const mongoose = require("mongoose");

// This is an example schema used for reference
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
