const { default: axios } = require("axios");
const request = require("supertest");
const app = require("../../server");
const mockingoose = require("mockingoose");
const Review = require("../../models/Review");
const { ID_1_RESP } = require("./mock_response");

jest.mock("axios");

describe("/searchRestaurantInfoByID", () => {
  beforeEach(() => {
    mockingoose.resetAll();
  });

  const reviewDoc = {
    google_place_id: "ChIJ5UuDwMtyhlQRa2nfU9eAqRQ",
    user_name: "Scottie Barnes",
    review: "Food is great",
    rating: 5,
    user_id: "123",
    __v: 0,
  };

  it("test with no result field in response", async () => {
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

  it("test with a valid query string", async () => {
    mockingoose(Review).toReturn(reviewDoc, "find");
    axios.get.mockResolvedValue({
      data: {
        result: ID_1_RESP,
      },
    });

    const response = await request(app).get("/searchRestaurantInfoByID").query({
      id: "ChIJTYjW9soKhlQRwO6-7IRKVq0",
    });

    const receivedName = response.body.name;
    const receivedReviews = response.body.reviews;
    const expectedName = ID_1_RESP.name;
    const expectedReviews = reviewDoc;

    expect(receivedName).toEqual(expectedName);
    expect(receivedReviews.google_place_id).toEqual(
      expectedReviews.google_place_id
    );
    expect(receivedReviews.user_name).toEqual(expectedReviews.user_name);
    expect(response.statusCode).toBe(200);
  });
});
