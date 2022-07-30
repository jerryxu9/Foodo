const request = require("supertest");
const app = require("../../server");
const mockingoose = require("mockingoose");
const Review = require("../../models/review");

let ourGetMessagingDelete = require("../../utils/ourGetMessagingDelete");
jest.mock("../../utils/ourGetMessagingDelete", () => jest.fn());

describe("/deleteReview", () => {
  const deletedReviewDoc = {
    acknowledged: true,
    deletedCount: 1,
  };

  const failedReviewDoc = {
    acknowledged: true,
    deletedCount: 0,
  };

  beforeEach(() => {
    mockingoose.resetAll();
  });

  it("should delete a reivew", async () => {
    mockingoose(Review).toReturn(deletedReviewDoc, "deleteOne");
    ourGetMessagingDelete.mockImplementation(async () => {
      return new Promise((resolve) => {
        resolve("Success");
      });
    });

    const reqBody = {
      id: "123",
      position: "5",
      google_place_id: "ChIJ5UuDwMtyhlQRa2nfU9eAqRQ",
    };

    const resp = await request(app).delete("/deleteReview").send(reqBody);
    const respBody = resp.body;

    expect(respBody.acknowledged).toBe(true);
    expect(respBody.deletedCount).toBe(1);
    expect(resp.statusCode).toBe(200);
  });

  it("should not delete a non-existing review", async () => {
    mockingoose(Review).toReturn(failedReviewDoc, "deleteOne");
    ourGetMessagingDelete.mockImplementation(async () => {
      return new Promise((resolve) => {
        resolve("Success");
      });
    });

    const reqBody = {
      id: "456",
      position: "5",
      google_place_id: "ChIJ5UuDwMtyhlQRa2nfU9eAqRQ",
    };

    const resp = await request(app).delete("/deleteReview").send(reqBody);
    const respBody = resp.body;

    expect(respBody.acknowledged).toBe(true);
    expect(respBody.deletedCount).toBe(0);
    expect(resp.statusCode).toBe(200);
  });
});
