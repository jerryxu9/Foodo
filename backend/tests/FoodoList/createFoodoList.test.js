const request = require("supertest");
const app = require("../../server");
const mongoose = require("mongoose");
const mockingoose = require("mockingoose");
const User = require("../../models/User");
const { FoodoListModel } = require("../../models/FoodoList");

/* If you want to use the data in local mongodb, uncommment below and comment out the mocked one */
// describe("/createFoodoList", () => {
//   beforeEach(() => {
//     mongoose.connect("mongodb://localhost:27017/cpen321");
//   });

//   afterEach(() => {
//     mongoose.disconnect();
//   });

//   it("non-exsiting user", async () => {
//     const body = { userID: "non_exisiting_user", listName: "test" };
//     const expectedBody = { error: "User not found!" };
//     const response = await request(app).post("/createFoodoList").send(body);

//     expect(response.body).toStrictEqual(expectedBody);
//     expect(response.statusCode).toBe(200);
//   });
// });

/* Using mockingoose to mock the User model */
describe("/createFoodoList", () => {
  const userDoc = {
    _id: "123",
    name: "Scottie Barnes",
    email: "sbarnes@gmail.com",
  };

  beforeEach(() => {
    mockingoose.resetAll();
  });

  // will fail because we need to update the endpoint to handle the error
  it("test with a non-existing user", async () => {
    mockingoose(User).toReturn(userDoc, "findById");

    const reqBody = { userID: "non_exisiting_user", listName: "test" };
    const expectedBody = { error: "User not found!" };
    const response = await request(app).post("/createFoodoList").send(reqBody);

    expect(response.body).toStrictEqual(expectedBody);
    expect(response.statusCode).toBe(404); // need to change to 404 according to m6
  });

  it("test with an existing user", async () => {
    mockingoose(User).toReturn(userDoc, "findOne"); // tried find_by_id which didn't work, not sure why

    const reqBody = { userID: "123", listName: "Asian" };
    const expectedName = "Asian";
    const expectedUsers = ["123"];

    const response = await request(app).post("/createFoodoList").send(reqBody);

    expect(response.body?.name).toStrictEqual(expectedName);
    expect(response.body?.users).toStrictEqual(expectedUsers);
    expect(response.statusCode).toBe(200);
  });

  // will fail because we need to update the endpoint to handle the error
  // NOTE: THIS TEST CASE WILL TIME OUT BECAUSE MONGODB ALREADY CHECKS IF LISTNAME IS EMPTY
  // So it should be pass once we add error handling to prevent adding a list with empty name string
  // If you want to see it finishes, change the listName on line 73 to be "ANYTHING" but ""
  it("test with empty string for list name", async () => {
    mockingoose(User).toReturn(userDoc, "findOne"); // tried find_by_id which didn't work, not sure why

    const reqBody = { userID: "123", listName: "" };
    const expectedBody = { error: "Empty string for name" };

    const response = await request(app).post("/createFoodoList").send(reqBody);
    expect(response.body).toStrictEqual(expectedBody);
    expect(response.statusCode).toBe(400);
  });
});
