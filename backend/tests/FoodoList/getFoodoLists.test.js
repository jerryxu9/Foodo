const request = require("supertest");
const app = require("../../server");
const mockingoose = require("mockingoose");
const User = require("../../models/User");
const { FoodoListModel } = require("../../models/FoodoList");

describe("/getFoodoLists", () => {
  const foodoListId = "62e167d0af78853605d0f435";
  const restaurantId = "62e167d0af78853605d0f436";

  const userDoc = {
    _id: "123",
    name: "Scottie Barnes",
    email: "sbarnes@gmail.com",
  };

  const foodoListDocs = [
    {
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
    },
  ];

  beforeEach(() => {
    mockingoose.resetAll();
  });

  it("test with a non-existing user", async () => {
    mockingoose(User).toReturn(userDoc, "findById");

    const queryBody = { userID: "non_exisiting_user" };
    const expectedBody = { error: "User not found" };
    const response = await request(app).get("/getFoodoLists").query(queryBody);

    expect(response.body).toStrictEqual(expectedBody);
    expect(response.statusCode).toBe(404);
  });

  it("test with an existing user", async () => {
    mockingoose(User).toReturn(userDoc, "findOne");
    mockingoose(FoodoListModel).toReturn(foodoListDocs, "find");

    const queryBody = { userID: "123" };
    const response = await request(app).get("/getFoodoLists").query(queryBody);

    expect(response.body).toStrictEqual(foodoListDocs);
    expect(response.statusCode).toBe(200);
  });
});
