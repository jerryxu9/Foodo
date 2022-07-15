const { default: axios } = require("axios");
const request = require("supertest");
const app = require("../../server");
const { QUERY_1_RESP } = require("./mock_response");

jest.mock("axios");

describe("/searchRestaurantsByQuery", () => {
  it("test with a valid query string", async () => {
    axios.get.mockResolvedValue({
      data: QUERY_1_RESP,
    });

    const response = await request(app).get("/searchRestaurantsByQuery").query({
      query: "chinese food in downtown vancouver",
      lat: 49.28272833333334,
      lng: -123.12073666666666,
    });

    expect(response.header["content-type"]).toBe(
      "application/json; charset=utf-8"
    );

    expect(response.body.length).toEqual(20);
    expect(response.body).toEqual(QUERY_1_RESP);
    expect(response.statusCode).toBe(200);
  });
});
