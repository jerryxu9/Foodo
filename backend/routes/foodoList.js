const express = require("express");
const router = express.Router();
const { FoodoRestaurantModel } = require("../models/FoodoRestaurant");
const { FoodoListModel } = require("../models/FoodoList");
const User = require("../models/User");

// Create a new FoodoList
router.post("/createFoodoList", async (req, res) => {
  const user = await User.findById(req.body?.userID);
  if (!user) {
    res.statusCode = 404;
    res.json({ error: "User not found!" });
  } else if (req.body?.listName === "") {
    res.statusCode = 400;
    res.json({ error: "Empty string for list name" });
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
  // Find the user first
  const user = await User.findById(req.query?.userID);
  if (!user) {
    res.status(404).json({ error: "User not found" });
  } else {
    FoodoListModel.find({ users: req.query?.userID })
      .then((lists) => {
        res.json(lists);
      })
      .catch((err) => {
        res.json(err);
      });
  }
});

// Get all the restaurants under a Foodo list given its ID
router.get("/getRestaurantsByFoodoListID", async (req, res) => {
  FoodoListModel.findById(req.query?.listID)
    .then((list) => {
      const restaurants = list.restaurants;
      res.json(restaurants);
    })
    .catch((err) => {
      res.json(err);
    });
});

// Delete a Foodo list from db
router.delete("/deleteFoodoList", async (req, res) => {
  FoodoListModel.findByIdAndDelete(req.body?.listID)
    .then((removedList) => {
      // if list is not found
      if (removedList == null) {
        res.status(404).json({ error: "List not found" });
      } else {
        res.json(removedList);
      }
    })
    .catch((err) => {
      res.json(err);
    });
});

// Add a restaurant to a Foodo list
router.patch("/addRestaurantToList", async (req, res) => {
  // check lat/lon values
  if (
    req.body?.lat < -90 ||
    req.body?.lat > 90 ||
    req.body?.lng < -180 ||
    req.body?.lng > 180
  ) {
    res.statusCode = 400;
    res.json({ error: "Invalid latitude/longitude values" });
  } else if (req.body?.restaurantID === "-1") {
    res.statusCode = 404;
    res.json({ error: "Invalid restaurant ID, restaurant could not be found" });
  } else if (req.body?.restaurantName === "") {
    res.statusCode = 400;
    res.json({
      error: "Invalid restaurant name, restaurant name cannot be empty",
    });
  } else {
    const newRestaurant = new FoodoRestaurantModel({
      place_id: req.body?.restaurantID,
      name: req.body?.restaurantName,
      isVisited: req.body?.isVisited,
      lat: req.body?.lat,
      lng: req.body?.lng,
    });

    const pushItem = { restaurants: newRestaurant };

    FoodoListModel.findByIdAndUpdate(
      req.body?.listID,
      {
        $push: pushItem,
      },
      {
        returnDocument: "after",
      }
    )
      .then((updatedList) => {
        if (updatedList === null) {
          res.statusCode = 404;
          res.json({ error: "List not found" });
        } else res.json(updatedList);
      })
      .catch((err) => {
        res.json(err);
      });
  }
});

// Delete a restaurant from a Foodo list
router.patch("/deleteRestaurantFromList", async (req, res) => {
  const restaurant = { _id: req.body?.restaurantID };
  const pullItem = { restaurants: restaurant };

  FoodoListModel.findByIdAndUpdate(
    req.body?.listID,
    {
      $pull: pullItem,
    },
    {
      returnDocument: "after",
    }
  )
    .then((updatedList) => {
      if (updatedList === null) {
        res.statusCode = 404;
        res.json({ error: "List not found" });
      } else res.json(updatedList);
    })
    .catch((err) => {
      res.json(err);
    });
});

// Add a user to the users array in a Foodo list
router.patch("/addNewUserToList", async (req, res) => {
  User.find({ email: req.body.email })
    .then((userList) => {
      if (userList.length === 0) {
        res.statusCode = 404;
        res.json({ error: "User not found!" });
      } else {
        const user = userList[0];
        const pushItem = { users: user._id };

        FoodoListModel.findByIdAndUpdate(
          req.body.listID,
          { $push: pushItem },
          {
            returnDocument: "after",
          }
        )
          .then((updatedList) => {
            res.json(updatedList);
          })
          .catch((err) => {
            res.json(err);
          });
      }
    })
    .catch((err) => {
      res.json(err);
    });
});

// Set the "isValid" field of a Foodo Restaurant as isVisited
router.patch("/checkRestaurantOnList", async (req, res) => {
  const setItem = { "restaurants.$.isVisited": req.body?.isVisited };

  FoodoListModel.findOneAndUpdate(
    {
      _id: req.body.listID,
      restaurants: { $elemMatch: { _id: req.body?.restaurantID } },
    },
    {
      $set: setItem,
    },
    {
      returnDocument: "after",
    }
  )
    .then((updatedList) => {
      res.json(updatedList);
    })
    .catch((err) => {
      res.json(err);
    });
});

module.exports = router;
