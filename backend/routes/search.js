const express = require("express");
const router = express.Router();
const axios = require("axios").default;
const parseRestResult = require("../utils/parseRestResult");
const ex_text_search_result = require("../utils/textSearchResult");

let near_by_search_string =
  "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=-33.8670522%2C151.1957362&radius=1500&type=restaurant&keyword=cruise&key=" +
  process.env.GOOGLE_PLACES_API_KEY;

let frontend_input = "restaurants%20near%20UBC";
let text_search_string =
  "https://maps.googleapis.com/maps/api/place/textsearch/json?query=" +
  frontend_input +
  "&type=restaurant" + // setting type to restaurant to only return restaurants
  "&radius=3000" + // limitting max radius to 3km to limit the results returned
  "&key=" +
  process.env.GOOGLE_PLACES_API_KEY;

// Get restaurants info by query
router.get("/searchRestaurantsByQuery", async (req, res) => {
  //   try {
  //     const response = await axios.get(text_search_string);
  //     if (response?.data?.results) {
  //       const parsed_data = parseRestResult(response?.data?.results);
  //       console.log(parsed_data);
  //     }
  //     res.json(response.data);
  //   } catch (err) {
  //     console.error(err);
  //     res.json(err);
  //   }
  const parsed_data = parseRestResult(ex_text_search_result);
  res.json(parsed_data);
  // res.json(`"data": ${parsed_data}`);
});

// TODO: Get restaurant info detail by restaurant id
router.get("/searchRestaurantInfoByID", async (req, res) => {});

module.exports = router;
