const request = require("supertest");
const app = require("../../server");
const mockingoose = require("mockingoose");
const { FoodoListModel } = require("../../models/FoodoList");

describe("/getRestaurantsByFoodoListID", () => {
  const foodoListId = "62e167d0af78853605d0f435";
  const restaurantId = "62e167d0af78853605d0f436";

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

  beforeEach(() => {
    mockingoose.resetAll();
  });

  // will fail because we need to update the endpoint to handle the error
  it("test list is not found", async () => {
    mockingoose(FoodoListModel).toReturn(null, "findOne");
    const queryBody = { listID: "non_exisiting_id" };
    const expectedBody = { error: "List not found" };
    const response = await request(app)
      .get("/getRestaurantsByFoodoListID")
      .query(queryBody);

    expect(response.body).toStrictEqual(expectedBody);
    expect(response.statusCode).toBe(404); // need to change to 404 according to m6
  });

  it("test successfully get restaurants", async () => {
    mockingoose(FoodoListModel).toReturn(foodoListDoc, "findOne");

    const queryBody = { userID: "123" };
    const expectedBody = [
      {
        _id: restaurantId,
        place_id: "90",
        name: "Uncle Tetsu",
        isVisited: true,
        lat: 10,
        lng: -10,
      },
    ];

    const response = await request(app)
      .get("/getRestaurantsByFoodoListID")
      .query(queryBody);

    expect(response.body).toStrictEqual(expectedBody);
    expect(response.statusCode).toBe(200);
  });
});
