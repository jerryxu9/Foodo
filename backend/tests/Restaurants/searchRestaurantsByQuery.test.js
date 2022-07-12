const request = require("supertest");
const app = require("../../server");
const { QUERY_1_RESP } = require("./mock_response");

describe("/searchRestaurantsByQuery", () => {
  it("test with a valid query string", async () => {
    const response = await request(app).get("/searchRestaurantsByQuery").query({
      query: "chinese food in downtown vancouver",
      lat: 49.28272833333334,
      lng: -123.12073666666666,
    });

    expect(response.header["content-type"]).toBe(
      "application/json; charset=utf-8"
    );
    expect(response.body).toEqual(QUERY_1_RESP);
    expect(response.body.length).toEqual(20);
    expect(response.statusCode).toBe(200);
  });
});
