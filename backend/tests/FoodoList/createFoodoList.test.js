const request = require("supertest");
const app = require("../../server");
const mongoose = require("mongoose");

describe("/createFoodoList", () => {
  beforeEach(() => {
    mongoose.connect("mongodb://localhost:27017/cpen321");
  });

  afterEach(() => {
    mongoose.disconnect();
  });

  it("test with a valid query string", async () => {
    const body = { userID: "non_exisiting_user", listName: "test" };
    const expectedBody = { error: "User not found!" };
    const response = await request(app).post("/createFoodoList").send(body);

    expect(response.body).toStrictEqual(expectedBody);
    expect(response.statusCode).toBe(200);
  });
});
