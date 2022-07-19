const { default: axios } = require("axios");
const request = require("supertest");
const app = require("../../server");
const { ID_1_RESP } = require("./mock_response");

jest.mock("axios");

describe("/searchRestaurantInfoByID", () => {
  it("test with a valid query string", async () => {
    axios.get.mockResolvedValue({
      data: {
        name: ID_1_RESP.name,
      },
    });

    const response = await request(app).get("/searchRestaurantInfoByID").query({
      id: "ChIJTYjW9soKhlQRwO6-7IRKVq0",
    });

    expect(response.body.name).toEqual(ID_1_RESP.name);
    expect(response.statusCode).toBe(200);
  });
});
