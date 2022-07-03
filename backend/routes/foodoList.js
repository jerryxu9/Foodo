const express = require("express");
const router = express.Router();
const {
  FoodoRestaurantSchema,
  FoodoRestaurantModel,
} = require("../models/FoodoRestaurant");
const { FoodoListModel } = require("../models/FoodoList");
const { UserAccountModel } = require("../models/UserAccount");

// Create a new FoodoList
// TODO!!!!!!!!!!!!!!
router.post("/createFoodoList", async (req, res) => {
  try {
    // NOTE: We should probably change this to find an existing user account instead of creating a new one!!!
    const userAccount = new UserAccountModel({
      name: req.body.userName,
      email: req.body.email,
    });

    const newList = new FoodoListModel({
      name: req.body.listName,
      users: [userAccount],
    });

    const response = await newList.save();
    console.log(response);
    res.json(response);
  } catch (err) {
    console.log(err);
    res.json(err);
  }
});

// Get all the Foodo lists of a user
// TODO!!!!!!!!!!!
router.get("/getFoodoLists", async (req, res) => {});

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
// TODO
router.patch("/addRestaurantToList", async (req, res) => {
  try {
    // might need to create a foodorestaurant object and store it in foodorestaurant db?
    // rn it's just an object
    // const newRestaurant = {
    //   place_id: req.body.restaurantID,
    //   name: req.body.restaurantName,
    //   isVisited: req.body.isVisited,
    // };
    const newRestaurant = new FoodoRestaurantModel({
      place_id: req.body.restaurantID,
      name: req.body.restaurantName,
      isVisited: req.body.isVisited,
    });

    const updatedList = await FoodoListModel.findByIdAndUpdate(
      req.body.listID,
      { $push: { restaurants: newRestaurant } }
    );

    res.json(updatedList);
  } catch (err) {
    res.json(err);
  }
});

// Delete a restaurant from a Foodo list
router.patch("/deleteRestaurantFromList", async (req, res) => {
  try {
    const lastList = await FoodoListModel.findByIdAndUpdate(req.body.listID, {
      $pull: {
        restaurants: {
          _id: req.body.restaurantID,
        },
      },
    });
    res.json(lastList);
  } catch (err) {
    res.json(err);
  }
});

// Add a user to the users array in a Foodo list
// TODO!!!!!!!!!
router.patch("/addNewUserToList", async (req, res) => {});

// Set the "isValid" field of a Foodo Restaurant as isVisited
router.patch("/checkRestaurantOnList", async (req, res) => {
  try {
    const lastList = await FoodoListModel.findOneAndUpdate(
      {
        _id: req.body.listID,
        restaurants: { $elemMatch: { _id: req.body.restaurantID } },
      },
      {
        $set: {
          "restaurants.$.isVisited": req.body.isVisited,
        },
      }
    );

    res.json(lastList);
  } catch (err) {
    res.json(err);
  }
});

module.exports = router;
