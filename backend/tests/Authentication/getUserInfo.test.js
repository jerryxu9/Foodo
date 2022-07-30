const request = require("supertest");
const app = require("../../server");
const mockingoose = require("mockingoose");
const User = require("../../models/User");

/* Using mockingoose to mock the User and FoodoList models */
describe("/createUserAccount", () => {
  const userDoc = {
    _id: "123",
    name: "Scottie Barnes",
    email: "sbarnes@gmail.com",
  };

  beforeEach(() => {
    mockingoose.resetAll();
  });

  it("test non-existent user by id", async () => {
    mockingoose(User).toReturn([], "find");

    const reqBody = { id: "123" };
    const expectedBody = { error: "User not found" };
    const response = await request(app).get("/getUser").send(reqBody);

    expect(response.body).toStrictEqual(expectedBody);
    expect(response.statusCode).toBe(404);
  });

  it("test existent user by id", async () => {
    mockingoose(User).toReturn([userDoc], "find");

    const reqBody = { id: "123" };
    const expectedBody = [userDoc];
    const response = await request(app).get("/getUser").send(reqBody);

    expect(response.body).toStrictEqual(expectedBody);
    expect(response.statusCode).toBe(200);
  });

  it("test non-existent user by email", async () => {
    mockingoose(User).toReturn([], "find");

    const reqBody = { email: "sbarnes@gmail.com" };
    const expectedBody = { error: "User not found" };
    const response = await request(app).get("/getUserByEmail").send(reqBody);

    expect(response.body).toStrictEqual(expectedBody);
    expect(response.statusCode).toBe(404);
  });

  it("test existent user by email", async () => {
    mockingoose(User).toReturn([userDoc], "find");

    const reqBody = { email: "sbarnes@gmail.com" };
    const expectedBody = [userDoc];
    const response = await request(app).get("/getUserByEmail").send(reqBody);

    expect(response.body).toStrictEqual(expectedBody);
    expect(response.statusCode).toBe(200);
  });
});
