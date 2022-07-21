const express = require("express");
const router = express.Router();
const Hello = require("../models/Hello");

// This file contains examples for reference

// Get all hello messages
router.get("/", async (req, res) => {
  Hello.find()
    .then((hellos) => {
      res.json(hellos);
    })
    .catch((err) => {
      res.json(err);
    });
});

// Post a new hello message
router.post("/", async (req, res) => {
  const hello = new Hello({
    title: req.body?.title,
    message: req.body?.message,
  });
  // Save this hello message to database
  const data = await hello.save();
  res.json(data);
});

// Find a specific hello message by title
router.get("/findHello", async (req, res) => {
  Hello.findOne({ title: req.query?.title })
    .then((hello) => {
      res.json(hello);
    })
    .catch((err) => {
      res.json(err);
    });
});

// Delete a hello message
router.delete("/", async (req, res) => {
  Hello.deleteOne({ _id: req.body?.id })
    .then((removedHello) => {
      res.json(removedHello);
    })
    .catch((err) => {
      res.json(err);
    });
});

// Update the message part of a hello message
router.patch("/", async (req, res) => {
  const setItem = { message: req.body?.message };

  Hello.findOneAndUpdate({ title: req.body?.title }, { $set: setItem })
    .then((updatedHello) => {
      res.json(updatedHello);
    })
    .catch((err) => {
      res.json(err);
    });
});

module.exports = router;
