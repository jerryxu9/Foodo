const request = require("supertest");
const app = require("../../server");
const mockingoose = require("mockingoose");
const User = require("../../models/User");
const UserModule = require("../../routes/user");
const verify = require("../../utils/verifyToken");

jest.mock("../../utils/verifyToken", () => jest.fn());

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

  it("test creating a user account for an existing user", async () => {
    verify.mockImplementation(
      async () => new Promise((resolve, reject) => resolve("123"))
    );
    mockingoose(User).toReturn(userDoc, "findOne");

    const reqBody = { id: "123" };
    const response = await request(app).post("/createUser").send(reqBody);

    expect(response.body).toStrictEqual(userDoc);
    expect(response.statusCode).toBe(200);
  });

  it("test creating a user account for a new user", async () => {
    verify.mockImplementation(
      async () => new Promise((resolve, reject) => resolve("012"))
    );

    mockingoose(User).toReturn(null, "find");

    const reqBody = {
      id: "012",
      name: "Serge Ibaka",
      email: "serge@gmail.com",
    };
    const expectedBody = {
      _id: "012",
      name: "Serge Ibaka",
      email: "serge@gmail.com",
    };
    const response = await request(app).post("/createUser").send(reqBody);

    expect(response.body).toStrictEqual(expectedBody);
    expect(response.statusCode).toBe(200);
  });
});
