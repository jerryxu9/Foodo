const request = require("supertest");
const app = require("../../server");
const mockingoose = require("mockingoose");
const Review = require("../../models/Review");

let ourGetMessagingAdd = require("../../utils/ourGetMessagingAdd");
jest.mock("../../utils/ourGetMessagingAdd", () => jest.fn());

describe("/addReview", () => {
  const reviewDoc = {
    google_place_id: "ChIJ5UuDwMtyhlQRa2nfU9eAqRQ",
    user_name: "Scottie Barnes",
    review: "Food is great",
    rating: 5,
    user_id: "123",
    __v: 0,
  };

  beforeEach(() => {
    mockingoose.resetAll();
  });

  it("should add a new reivew", async () => {
    ourGetMessagingAdd.mockImplementation(async () => {
      return new Promise((resolve) => {
        resolve("Success");
      });
    });

    mockingoose(Review).toReturn(reviewDoc, "findById");
    const reqBody = {
      google_place_id: "ChIJ5UuDwMtyhlQRa2nfU9eAqRQ",
      user_name: "Scottie Barnes",
      review: "Food is great",
      rating: 5,
      user_id: "123",
    };

    const resp = await request(app).post("/addReview").send(reqBody);
    const respBody = resp.body;

    const expectedBody = reviewDoc;
    expect(respBody.google_place_id).toStrictEqual(
      expectedBody.google_place_id
    );
    expect(respBody.user_name).toStrictEqual(expectedBody.user_name);
    expect(respBody.review).toStrictEqual(expectedBody.review);
    expect(respBody.rating).toStrictEqual(expectedBody.rating);
    expect(respBody.user_id).toStrictEqual(expectedBody.user_id);
    expect(resp.statusCode).toBe(200);
  });
});
