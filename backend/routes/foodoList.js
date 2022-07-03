const express = require("express");
const router = express.Router();
const {
  FoodoRestaurantSchema,
  FoodoRestaurantModel,
} = require("../models/FoodoRestaurant");
const { FoodoListModel } = require("../models/FoodoList");
const { UserAccountModel } = require("../models/UserAccount");

// Create a new FoodoList
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
// Modified from M4 (passed in place_id insteaad of restaurant id cuz it should be easier? like you dont need to find the restaurant id first)
router.patch("/deleteRestaurantFromList", async (req, res) => {
  try {
    // 1. try to find the restaurant by id using elemMatch (didn't work) (BECAUSE foodlistschema was using foodorestaurant schema?)
    // const updatedList = await FoodoListModel.findByIdAndUpdate(
    //   req.body.listID,
    //   {
    //     $pull: { restaurants: { $elemMatch: { _id: req.body.restaurantID } } },
    //   }
    // );
    const updatedList = await FoodoListModel.findByIdAndUpdate(
      req.body.listID,
      {
        $pull: {
          restaurants: {
            place_id: req.body.place_id,
          },
        },
      }
    );
    console.log(updatedList);
    res.json(updatedList);
  } catch (err) {
    console.log(err);
    res.json(err);
  }
});

// Add a user to the users array in a Foodo list
router.patch("/addNewUserToList", async (req, res) => {});

// Set the "isValid" field of a Foodo Restaurant as isVisited
router.patch("/checkRestaurantOnList", async (req, res) => {});

// Get the ID associated with the FoodoList (Find by list name and users)
router.get("/getListID", async (req, res) => {
  try {
    const foodoList = await FoodoListModel.findOne({
      name: req.body.name,
      users: req.body.users,
    });
    res.json(foodoList);
  } catch (err) {
    res.json(err);
  }
});

// Get the ID associated with the restaurant listed in the given FoodoRestaurant
router.get("/getRestaurantID", async (req, res) => {});

module.exports = router;
