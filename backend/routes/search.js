const express = require("express");
const router = express.Router();
const axios = require("axios").default;
const parseRestResult = require("../utils/parseRestResult");

// Get restaurants info by query
router.get("/searchRestaurantsByQuery", async (req, res) => {
  try {
    // Note: text_search returns max of 20 results unless passing in next_page_token to a subsequent request
    let text_search_string =
      "https://maps.googleapis.com/maps/api/place/textsearch/json?query=" +
      req.query.query +
      "&type=restaurant" + // setting type to restaurant to only return restaurants
      "&radius=3000" + // limitting max radius to 3km to limit the results returned
      "&key=" +
      process.env.GOOGLE_PLACES_API_KEY;

    const response = await axios.get(text_search_string);

    if (response?.data?.results) {
      const parsed_data = parseRestResult(response?.data?.results); // parse the response
      res.json(parsed_data);
    } else {
      res.json(response?.data);
    }
  } catch (err) {
    res.json(err);
  }
});

// Get restaurant info detail by restaurant id
router.get("/searchRestaurantInfoByID", async (req, res) => {
  try {
    let place_details_string =
      "https://maps.googleapis.com/maps/api/place/details/json?place_id=" +
      req.query.id +
      "&fields=place_id,name,formatted_address,business_status,opening_hours/open_now,opening_hours/weekday_text,rating,formatted_phone_number,geometry/location" +
      "&key=" +
      process.env.GOOGLE_PLACES_API_KEY;

    const response = await axios.get(place_details_string);

    if (response?.data?.result) {
      res.json(response.data.result);
    } else {
      res.json(response?.data);
    }
  } catch (err) {
    res.json(err);
  }
});

module.exports = router;
