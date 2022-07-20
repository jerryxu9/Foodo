const express = require("express");
const router = express.Router();
const Hello = require("../models/Hello");

// This file contains examples for reference

// Get all hello messages
router.get("/", async (req, res) => {
  const hellos = await Hello.find();
  res.json(hellos);
});

// Post a new hello message
router.post("/", async (req, res) => {
  try {
    const hello = new Hello({
      title: req.body.title,
      message: req.body.message,
    });
    // Save this hello message to database
    const data = await hello.save();
    res.json(data);
  } catch (err) {
    res.json(err);
  }
});

// Find a specific hello message by title
router.get("/findHello", async (req, res) => {
  try {
    const hello = await Hello.findOne({ title: req.query.title });
    res.json(hello);
  } catch (err) {
    res.json(err);
  }
});

// Delete a hello message
router.delete("/", async (req, res) => {
  try {
    const removedHello = await Hello.deleteOne({ _id: req.body.id });
    res.json(removedHello);
  } catch (err) {
    res.json(err);
  }
});

// Update the message part of a hello message
router.patch("/", async (req, res) => {
  try {
    const setItem = { message: req.body.message };

    const updatedHello = await Hello.findOneAndUpdate(
      { title: req.body.title },
      { $set: setItem }
    );
    res.json(updatedHello);
  } catch (err) {
    res.json(err);
  }
});

module.exports = router;
