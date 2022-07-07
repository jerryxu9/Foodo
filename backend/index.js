const express = require("express");
const mongoose = require("mongoose");
const bodyParser = require("body-parser");
const cors = require("cors");
require("dotenv").config();

const app = express();

/* Middleware */
app.use(cors());
app.use(bodyParser.json());

/* Route middleware */
// Example route
const helloRoute = require("./routes/hello");
app.use("/hello", helloRoute);
// Review route
const reviewRoute = require("./routes/review");
app.use("/", reviewRoute);
// Search route
const searchRoute = require("./routes/search");
app.use("/", searchRoute);
// User route
const userRoute = require("./routes/user");
app.use("/", userRoute);
// Foodo list route
const foodoListRoute = require("./routes/foodoList");
app.use("/", foodoListRoute);

// Routes
// The home page route is used as a quick check to see if server is running
app.get("/", (req, res) => {
  res.send("Home page!!");
});

async function main() {
  try {
    await mongoose.connect("mongodb://localhost:27017/cpen321");
    console.log("Successfully connected to the CPEN 321 database");

    const server = app.listen(3000, (req, res) => {
      console.log("Server successfully running at port 3000");
    });
  } catch (err) {
    console.log(err);
    await mongoose.disconnect();
  }
}

main().catch((err) => console.log(err));
