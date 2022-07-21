const express = require("express");
const router = express.Router();
const User = require("../models/User");
const { OAuth2Client } = require("google-auth-library");
const client = new OAuth2Client(
  "415243569715-ft0h81psvpkm3ufbc9h5qlkomrd1k8bp.apps.googleusercontent.com"
);

async function verify(token) {
  return client
    .verifyIdToken({
      idToken: token,
      audience:
        "415243569715-ft0h81psvpkm3ufbc9h5qlkomrd1k8bp.apps.googleusercontent.com",
    })
    .then((ticket) => {
      const payload = ticket.getPayload();
      const userid = payload["sub"];
      return userid;
    });
}

router.post("/createUser", async (req, res) => {
  verify(req.body.id)
    .then(async (userID) => {
      console.log(userID);
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
    })
    .catch((err) => {
      console.log(err);
      res.json({ error: "validation error" });
    });
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
