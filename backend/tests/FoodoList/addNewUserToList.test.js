const request = require("supertest");
const app = require("../../server");
const mockingoose = require("mockingoose");
const User = require("../../models/User");
const { FoodoListModel } = require("../../models/FoodoList");

/* Using mockingoose to mock the User and FoodoList models */
describe("/addNewUserToList", () => {
  const foodoListId = "62e167d0af78853605d0f435";
  const restaurantId = "62e167d0af78853605d0f436";

  const userDoc = {
    _id: "123",
    name: "Scottie Barnes",
    email: "sbarnes@gmail.com",
  };
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
    users: ["012", "123"],
  };

  beforeEach(() => {
    mockingoose.resetAll();
  });

  it("test successfully adding a user to a foodo list", async () => {
    mockingoose(User).toReturn([userDoc], "find");
    mockingoose(FoodoListModel).toReturn(foodoListDoc, "findOneAndUpdate");

    const reqBody = { email: "sbarnes@gmail.com", listID: "123" };
    const response = await request(app)
      .patch("/addNewUserToList")
      .send(reqBody);

    expect(response.body).toStrictEqual(foodoListDoc);
    expect(response.statusCode).toBe(200);
  });

  // Should fail since have not handled this error case yet
  it("test non-existent list ID", async () => {
    mockingoose(User).toReturn([userDoc], "find");
    mockingoose(FoodoListModel).toReturn(null, "findOneAndUpdate");

    const reqBody = { email: "sbarnes@gmail.com", listID: "123" };
    const expectedBody = { error: "List not found" };
    const response = await request(app)
      .patch("/addNewUserToList")
      .send(reqBody);

    expect(response.body).toStrictEqual(expectedBody);
    expect(response.statusCode).toBe(404);
  });

  it("test user not found", async () => {
    mockingoose(User).toReturn([], "find");

    const reqBody = { email: "sbarnes@gmail.com", listID: "123" };
    const expectedBody = { error: "User not found!" };

    const response = await request(app)
      .patch("/addNewUserToList")
      .send(reqBody);
    expect(response.body).toStrictEqual(expectedBody);
    expect(response.statusCode).toBe(404);
  });
});
