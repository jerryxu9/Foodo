const request = require("supertest");
const app = require("../../server");
const mockingoose = require("mockingoose");
const { FoodoListModel } = require("../../models/FoodoList");

/* Using mockingoose to mock the User model */
describe("/deleteFoodoList", () => {
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

  it("test list is not found", async () => {
    mockingoose(FoodoListModel).toReturn(null, "findOneAndDelete");

    const reqBody = { listID: "non_exisiting_list" };
    const expectedBody = { error: "List not found" };
    const response = await request(app)
      .delete("/deleteFoodoList")
      .send(reqBody);

    expect(response.body).toStrictEqual(expectedBody);
    expect(response.statusCode).toBe(404);
  });

  it("test successfully delete a list", async () => {
    mockingoose(FoodoListModel).toReturn(foodoListDoc, "findOneAndDelete");

    const reqBody = { listID: foodoListId };
    const response = await request(app)
      .delete("/deleteFoodoList")
      .send(reqBody);

    expect(response.body).toStrictEqual(foodoListDoc);
    expect(response.statusCode).toBe(200);
  });
});
