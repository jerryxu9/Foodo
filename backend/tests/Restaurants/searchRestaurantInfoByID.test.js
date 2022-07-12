const request = require("supertest");
const app = require("../../server");
const { ID_1_RESP } = require("./mock_response");

describe("/searchRestaurantInfoByID", () => {
  it("test with a valid query string", async () => {
    const response = await request(app).get("/searchRestaurantInfoByID").query({
      id: "ChIJTYjW9soKhlQRwO6-7IRKVq0",
    });

    expect(response.body).toEqual(ID_1_RESP);
    expect(response.statusCode).toBe(200);
  });
});
