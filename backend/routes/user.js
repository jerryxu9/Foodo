const express = require("express");
const router = express.Router();
const User = require("../models/User");
const verify = require("../utils/verifyToken");

router.post("/createUser", async (req, res) => {
  verify(req.body?.id)
    .then(async (userID) => {
      console.log(userID);
      const existingUser = await User.findById(userID);

      if (!existingUser) {
        const user = new User({
          _id: userID,
          name: req.body?.name,
          email: req.body?.email,
        });

        const data = await user.save();
        res.json(data);
      } else {
        res.json(existingUser);
      }
    })
    .catch((err) => {
      console.log(err);
      res.json({ error: "validation error" });
    });
});

router.get("/getUser", async (req, res) => {
  User.find({ _id: req.query?.id })
    .then((user) => {
      if (user.length === 0) {
        res.statusCode = 404;
        res.json({ error: "User not found" });
      } else res.json(user);
    })
    .catch((err) => {
      res.json(err);
    });
});

router.get("/getUserByEmail", async (req, res) => {
  User.find({ email: req.query?.email })
    .then((user) => {
      if (user.length === 0) {
        res.statusCode = 404;
        res.json({ error: "User not found" });
      } else res.json(user);
    })
    .catch((err) => {
      res.json(err);
    });
});

module.exports = router;
