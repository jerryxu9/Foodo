const mockingoose = require("mockingoose");
const Review = require("../../models/Review");
const getReviews = require("../../utils/getReview");

describe("getReviews", () => {
  beforeEach(() => {
    mockingoose.resetAll();
  });

  const google_place_id = "ChIJ5UuDwMtyhlQRa2nfU9eAqRQ";
  const reviewDocs = [
    {
      google_place_id: google_place_id,
      user_name: "Scottie Barnes",
      review: "Food is great",
      rating: 5,
      user_id: "123",
      __v: 0,
    },
    {
      google_place_id: google_place_id,
      user_name: "Kyle Lowry",
      review: "I like the Philly cheesesteak",
      rating: 5,
      user_id: "456",
      __v: 0,
    },
  ];

  const emptyDoc = [];

  it("test with restaurant that has at least 1 review", async () => {
    mockingoose(Review).toReturn(reviewDocs, "find");

    const reviews = await getReviews(google_place_id);
    expect(reviews.length).toEqual(2);
    expect(reviews[0].user_name).toEqual(reviewDocs[0].user_name);
    expect(reviews[1].user_name).toEqual(reviewDocs[1].user_name);
    expect(reviews[0].review).toEqual(reviewDocs[0].review);
    expect(reviews[1].review).toEqual(reviewDocs[1].review);
  });

  it("test with restaurant that has no reviews", async () => {
    mockingoose(Review).toReturn(emptyDoc, "find");

    const reviews = await getReviews(google_place_id);
    expect(reviews.length).toEqual(0);
    expect(reviews).toEqual([]);
  });
});
