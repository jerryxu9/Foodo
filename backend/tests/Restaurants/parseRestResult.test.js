const parseRestResult = require("../../utils/parseRestResult");

jest.mock("../../utils/parseRestResult");

describe("parseRestResult", () => {
  it("should be called with empty array", async () => {
    const restaurants = [];
    parseRestResult(restaurants);
    expect(parseRestResult).toHaveBeenCalledWith([]);
  });
});
