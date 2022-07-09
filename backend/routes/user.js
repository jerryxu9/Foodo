const express = require("express");
const router = express.Router();
const User = require("../models/User");
const { OAuth2Client } = require("google-auth-library");
const client = new OAuth2Client(
  "415243569715-ft0h81psvpkm3ufbc9h5qlkomrd1k8bp.apps.googleusercontent.com"
);

async function verify(token) {
  try {
    const ticket = await client.verifyIdToken({
      idToken: token,
      audience:
        "415243569715-ft0h81psvpkm3ufbc9h5qlkomrd1k8bp.apps.googleusercontent.com",
    });
    const payload = ticket.getPayload();
    const userid = payload["sub"];
    return userid;
  } catch (err) {
    console.log(err);
  }
}

router.post("/createUser", async (req, res) => {
  try {
    const userID = await verify(req.body.id);

    if (!userID) {
      res.json({ error: "token invalid" }).status(400);
    } else {
      const existingUser = await User.findById(userID);

      if (!existingUser) {
        const user = new User({
          _id: userID,
          name: req.body.name,
          email: req.body.email,
        });

        const data = await user.save();
        res.json(data);
      } else {
        res.json(existingUser);
      }
    }
  } catch (error) {
    res.json(error);
  }
});

router.get("/getUser", async (req, res) => {
  const user = await User.find({ _id: req.query.id });
  res.json(user);
});

router.get("/getUserByEmail", async (req, res) => {
  const user = await User.find({ email: req.query.email });
  res.json(user);
});

module.exports = router;
