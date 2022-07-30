const { default: axios } = require("axios");
const request = require("supertest");
const app = require("../../server");
const {
  GOOGLE_TEXT_SEARCH_RESP,
  PARSED_GOOGLE_TEXT_SEARCH_RESP,
} = require("./mock_response");

jest.mock("axios");

describe("/searchRestaurantsByQuery", () => {
  it("test with a valid query string", async () => {
    // We need to set the mock resolved value to work with axios
    axios.get.mockResolvedValue({
      data: { results: GOOGLE_TEXT_SEARCH_RESP },
    });

    const response = await request(app).get("/searchRestaurantsByQuery").query({
      query: "chinese food in downtown vancouver",
      lat: 49.28272833333334,
      lng: -123.12073666666666,
    });

    expect(response.header["content-type"]).toBe(
      "application/json; charset=utf-8"
    );
    expect(response.body.length).toEqual(3);
    expect(response.body).toEqual(PARSED_GOOGLE_TEXT_SEARCH_RESP);
    expect(response.statusCode).toBe(200);
  });

  // will fail because we need to add error handling in the endpoint
  it("test with invalid lat/lng values", async () => {
    const expectedBody = { error: "Invalid latitude/longitude values" };
    const response = await request(app).get("/searchRestaurantsByQuery").query({
      query: "chinese food in downtown vancouver",
      lat: -100,
      lng: 200,
    });

    expect(response.body).toEqual(expectedBody);
    expect(response.statusCode).toBe(400); // will need to update the endpoint
  });

  it("test with no results field in the reponse body", async () => {
    // We need to set the mock resolved value to work with axios
    axios.get.mockResolvedValue({
      data: GOOGLE_TEXT_SEARCH_RESP,
    });

    const response = await request(app).get("/searchRestaurantsByQuery").query({
      query: "chinese food in downtown vancouver",
      lat: 49.28272833333334,
      lng: -123.12073666666666,
    });

    expect(response.header["content-type"]).toBe(
      "application/json; charset=utf-8"
    );
    expect(response.body.length).toEqual(3);
    expect(response.body).toEqual(GOOGLE_TEXT_SEARCH_RESP);
    expect(response.statusCode).toBe(200);
  });
});
