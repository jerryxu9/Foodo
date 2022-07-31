const request = require("supertest");
const app = require("../../server");
const mockingoose = require("mockingoose");
const { FoodoListModel } = require("../../models/FoodoList");

/* Using mockingoose to mock the User model */
describe("/checkRestaurantOnList", () => {
  beforeEach(() => {
    mockingoose.resetAll();
  });

  const foodoListId = "62e167d0af78853605d0f435";
  const restaurantId = "62e167d0af78853605d0f436";

  it("test successful deletion of a restaurant from foodo list", async () => {
    const foodoListDoc = {
      _id: foodoListId,
      name: "Deserts",
      restaurants: [],
      users: ["123"],
    };

    mockingoose(FoodoListModel).toReturn(foodoListDoc, "findOneAndUpdate");

    const reqBody = {
      listID: foodoListId,
      restaurantID: restaurantId,
    };
    const response = await request(app)
      .patch("/deleteRestaurantFromList")
      .send(reqBody);

    expect(response.body).toStrictEqual(foodoListDoc);
    expect(response.statusCode).toBe(200);
  });

  it("test list is not found", async () => {
    mockingoose(FoodoListModel).toReturn(null, "findOneAndUpdate");

    const reqBody = {
      listID: foodoListId,
      restaurantID: restaurantId,
    };
    const expectedBody = { error: "List not found" };
    const response = await request(app)
      .patch("/deleteRestaurantFromList")
      .send(reqBody);

    expect(response.body).toStrictEqual(expectedBody);
    expect(response.statusCode).toBe(404);
  });

  //   Will error since we need to implement error handling in this case
  it("test restaurant is not found in the list", async () => {
    const foodoListDoc = {
      _id: foodoListId,
      name: "Deserts",
      restaurants: [
        {
          _id: restaurantId,
          place_id: "90",
          name: "Uncle Tetsu",
          isVisited: true,
          lat: 10,
          lng: -10,
        },
      ],
      users: ["123"],
    };

    mockingoose(FoodoListModel).toReturn(foodoListDoc, "findOneAndUpdate");

    const reqBody = {
      listID: foodoListId,
      restaurantID: "012",
    };
    const expectedBody = { error: "Restaurant not found" };
    const response = await request(app)
      .patch("/checkRestaurantOnList")
      .send(reqBody);

    expect(response.body).toStrictEqual(expectedBody);
    expect(response.statusCode).toBe(404);
  });
});
