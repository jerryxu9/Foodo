const parseRestResult = require("../../utils/parseRestResult");
const {
  GOOGLE_TEXT_SEARCH_RESP,
  PARSED_GOOGLE_TEXT_SEARCH_RESP,
} = require("./mock_response");

describe("parseRestResult", () => {
  it("test with empty array", async () => {
    const restaurants = [];
    const expected = [];
    const result = parseRestResult(restaurants);
    expect(result).toEqual(expected);
  });

  it("test with an array of restaurants", async () => {
    const expected = PARSED_GOOGLE_TEXT_SEARCH_RESP;
    const result = parseRestResult(GOOGLE_TEXT_SEARCH_RESP);
    expect(result).toEqual(expected);
  });
});
