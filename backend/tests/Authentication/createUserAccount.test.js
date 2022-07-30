const request = require("supertest");
const app = require("../../server");
const mockingoose = require("mockingoose");
const User = require("../../models/User");
const UserModule = require("../../routes/user");

jest.mock("../../routes/user", () => ({
  ...jest.requireActual("../../routes/user"),
  verify: async () => "123",
}));

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
    jest.spyOn(UserModule, "verify").mockReturnValue("123");
    UserModule.verify.mockReturnValue("HIHI");
    console.log(UserModule.verify());
    mockingoose(User).toReturn(userDoc, "findOne");

    const reqBody = { id: "123" };
    const response = await request(app).post("/createUser").send(reqBody);

    expect(response.body).toStrictEqual(userDoc);
    expect(response.statusCode).toBe(200);
  });

  //   it("test creating a user account for a new user", async () => {
  //     jest.spyOn(UserModule, "verify").mockReturnValue("012");

  //     mockingoose(User).toReturn(null, "find");

  //     const reqBody = {
  //       id: "012",
  //       name: "Serge Ibaka",
  //       email: "serge@gmail.com",
  //     };
  //     const expectedBody = {
  //       _id: "012",
  //       name: "Serge Ibaka",
  //       email: "serge@gmail.com",
  //     };
  //     const response = await request(app).post("/createUser").send(reqBody);

  //     expect(response.body).toStrictEqual(expectedBody);
  //     expect(response.statusCode).toBe(200);
  //   });
});
