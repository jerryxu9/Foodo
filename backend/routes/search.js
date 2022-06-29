const express = require("express");
const router = express.Router();
const axios = require("axios").default;
const parseRestResult = require("../utils/parseRestResult");
const ex_text_search_result = require("../utils/textSearchResult");

let near_by_search_string =
  "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=-33.8670522%2C151.1957362&radius=1500&type=restaurant&keyword=cruise&key=" +
  process.env.GOOGLE_PLACES_API_KEY;

let frontend_input = "restaurants%20near%20UBC";
// Note: text_search returns max of 20 results unless passing in next_page_token to a subsequent request
let text_search_string =
  "https://maps.googleapis.com/maps/api/place/textsearch/json?query=" +
  frontend_input +
  "&type=restaurant" + // setting type to restaurant to only return restaurants
  "&radius=3000" + // limitting max radius to 3km to limit the results returned
  "&key=" +
  process.env.GOOGLE_PLACES_API_KEY;

// Get restaurants info by query
router.get("/searchRestaurantsByQuery", async (req, res) => {
  try {
    let frontend_input = "restaurants%20near%20UBC";
    const response = await axios.get(text_search_string);
    if (response?.data?.results) {
      const parsed_data = parseRestResult(response?.data?.results);
      console.log(parsed_data);
      res.json(parsed_data);
    } else {
      console.log("No results from Google Places API");
      res.json(response.data);
    }
  } catch (err) {
    console.error(err);
    res.json(err);
  }
});

// TODO: Get restaurant info detail by restaurant id
router.get("/searchRestaurantInfoByID", async (req, res) => {});

module.exports = router;
