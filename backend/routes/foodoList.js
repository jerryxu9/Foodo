const express = require("express");
const router = express.Router();
const { FoodoRestaurantModel } = require("../models/FoodoRestaurant");
const { FoodoListModel } = require("../models/FoodoList");
const User = require("../models/User");

// Create a new FoodoList
router.post("/createFoodoList", async (req, res) => {
  const user = await User.findById(req.body.userID);
  if (!user) {
    res.json({ error: "User not found!" });
  } else {
    const newList = new FoodoListModel({
      name: req.body.listName,
      users: [req.body.userID],
    });
    const response = await newList.save();
    res.json(response);
  }
});

// Get all the Foodo lists of a user
router.get("/getFoodoLists", async (req, res) => {
  const lists = await FoodoListModel.find({ users: req.query.userID });
  res.json(lists);
});

// Get all the restaurants under a Foodo list given its ID
router.get("/getRestaurantsByFoodoListID", async (req, res) => {
  try {
    const list = await FoodoListModel.findById(req.query.listID);
    const restaurants = list.restaurants;
    res.json(restaurants);
  } catch (err) {
    res.json(err);
  }
});

// Delete a Foodo list from db
router.delete("/deleteFoodoList", async (req, res) => {
  try {
    const removedList = await FoodoListModel.findByIdAndDelete(req.body.listID);
    res.json(removedList);
  } catch (err) {
    res.json(err);
  }
});

// Add a restaurant to a Foodo list
router.patch("/addRestaurantToList", async (req, res) => {
  try {
    const newRestaurant = new FoodoRestaurantModel({
      place_id: req.body.restaurantID,
      name: req.body.restaurantName,
      isVisited: req.body.isVisited,
      lat: req.body.lat,
      lng: req.body.lng,
    });

    const pushItem = { restaurants: newRestaurant };

    const updatedList = await FoodoListModel.findByIdAndUpdate(
      req.body.listID,
      {
        $push: pushItem,
      },
      {
        returnDocument: "after",
      }
    );

    res.json(updatedList);
  } catch (err) {
    res.json(err);
  }
});

// Delete a restaurant from a Foodo list
router.patch("/deleteRestaurantFromList", async (req, res) => {
  try {
    const restaurant = { _id: req.body.restaurantID };
    const pullItem = { restaurants: restaurant };

    const updatedList = await FoodoListModel.findByIdAndUpdate(
      req.body.listID,
      {
        $pull: pullItem,
      },
      {
        returnDocument: "after",
      }
    );
    res.json(updatedList);
  } catch (err) {
    res.json(err);
  }
});

// Add a user to the users array in a Foodo list
router.patch("/addNewUserToList", async (req, res) => {
  try {
    const user = await User.findById(req.body.userID);
    if (!user) {
      res.json({ error: "User not found!" });
    } else {
      const pushItem = { users: req.body.userID };

      const updatedList = await FoodoListModel.findByIdAndUpdate(
        req.body.listID,
        { $push: pushItem },
        {
          returnDocument: "after",
        }
      );

      res.json(updatedList);
    }
  } catch (err) {
    res.json(err);
  }
});

// Set the "isValid" field of a Foodo Restaurant as isVisited
router.patch("/checkRestaurantOnList", async (req, res) => {
  try {
    const setItem = { "restaurants.$.isVisited": req.body.isVisited };

    const updatedList = await FoodoListModel.findOneAndUpdate(
      {
        _id: req.body.listID,
        restaurants: { $elemMatch: { _id: req.body.restaurantID } },
      },
      {
        $set: setItem,
      },
      {
        returnDocument: "after",
      }
    );

    res.json(updatedList);
  } catch (err) {
    res.json(err);
  }
});

module.exports = router;
