const mongoose = require("mongoose");

const UserAccountSchema = mongoose.Schema({
  name: {
    type: String,
    required: true,
  },
  email: {
    type: String,
    required: true,
  },
});

const UserAccountModel = mongoose.model("UserAccount", UserAccountSchema);

module.exports = {
  UserAccountSchema,
  UserAccountModel,
};
