const express = require("express");
const router = express.Router();
const User = require("../models/User");

const { MongoClient } = require("mongodb");
const uri = "mongodb://localhost:27017";
const dbClient = new MongoClient(uri);

// Get user data if account exists, else create a new doc
// router.get("/getUser", async (req, res) => {
//   try {
//     const user = await User.findOneAndUpdate(
//       { sub: req.body.id },
//       {
//         new: true,
//         upsert: true,
//       }
//     );
//     res.json(user);
//   } catch (err) {
//     res.json(err);
//   }
// });

// https://developers.google.com/identity/sign-in/android/backend-auth
const { OAuth2Client } = require("google-auth-library");
const client = new OAuth2Client();

router.get("/getUser", async (req, res) => {
  console.log("got a request");
  try {
    const ticket = await client.verifyIdToken({
      idToken: req.id_token,
    });
    const payload = ticket.getPayload();
    const userid = payload["sub"];
    // If request specified a G Suite domain:
    // const domain = payload['hd'];
    console.log("User id is: " + userid);
    console.log("Yayyyy!!!!");
  } catch (err) {
    console.log(err);
  }
});

router.get("/createUser", async (req, res) => {
  const user = await dbClient.db("users").insert({ test: "haha" });
});

router.get("/getUser", async (req, res) => {
  const user = await dbClient.db("users").insert({ test: "haha" });
});

router.get("/findUserByEmail", async (req, res) => {
  const user = await dbClient.db("users").find({ email: req.body.email });
  res.send("work!!");
});

module.exports = router;
