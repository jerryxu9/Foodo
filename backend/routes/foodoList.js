const express = require("express");
const router = express.Router();
const { FoodoRestaurantModel } = require("../models/FoodoRestaurant");
const { FoodoListModel } = require("../models/FoodoList");
const User = require("../models/User");

// Create a new FoodoList
router.post("/createFoodoList", async (req, res) => {
  try {
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
  } catch (err) {
    console.log(err);
    res.json(err);
  }
});

// Get all the Foodo lists of a user
router.get("/getFoodoLists", async (req, res) => {
  try {
    const lists = await FoodoListModel.find({ users: req.query.userID });

    res.json(lists);
  } catch (err) {
    res.json(err);
  }
});

// Get all the restaurants under a Foodo list given its ID
router.get("/getRestaurantsByFoodoListID", async (req, res) => {
  try {
    const list = await FoodoListModel.findById(req.query.listID);
    const restaurants = list.restaurants;
    res.json(restaurants)
  } catch (err) {
    res.json(err);
  }
})

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

    const updatedList = await FoodoListModel.findByIdAndUpdate(
      req.body.listID,
      {
        $push: { restaurants: newRestaurant },
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
    const updatedList = await FoodoListModel.findByIdAndUpdate(
      req.body.listID,
      {
        $pull: {
          restaurants: {
            _id: req.body.restaurantID,
          },
        },
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
      const updatedList = await FoodoListModel.findByIdAndUpdate(
        req.body.listID,
        { $push: { users: req.body.userID } }, // note that M4 uses the name 'ID'
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
    const updatedList = await FoodoListModel.findOneAndUpdate(
      {
        _id: req.body.listID,
        restaurants: { $elemMatch: { _id: req.body.restaurantID } },
      },
      {
        $set: {
          "restaurants.$.isVisited": req.body.isVisited,
        },
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
