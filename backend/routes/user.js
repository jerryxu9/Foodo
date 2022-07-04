const express = require("express");
const router = express.Router();
const User = require("../models/User");

router.post("/createUser", async (req, res) => {
  try {
    const user = new User({
      _id: req.body.email,
      name: req.body.name,
      email: req.body.email,
    });
    const data = await user.save();
    res.json(data);
  } catch (error) {
    res.json(error);
  }
});

router.get("/getUser", async (req, res) => {
  const user = await User.find({ _id: req.query._id });
  res.json(user);
});

router.get("/getUserByEmail", async (req, res) => {
  const user = await User.find({ email: req.query.email });
  res.json(user);
});

module.exports = router;
