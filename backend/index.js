const express = require("express");
const mongoose = require("mongoose");
const bodyParser = require("body-parser");
const cors = require("cors");

const app = express();

// Middleware
app.use(cors());
app.use(bodyParser.json());

// Route middleware
const helloRoute = require("./routes/hello");
app.use("/hello", helloRoute);

// Routes
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
