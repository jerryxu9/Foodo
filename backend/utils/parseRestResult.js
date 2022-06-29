const parseRestResult = (restaurants) => {
  let parsed_data = [];

  restaurants.forEach((item) => {
    let parsed_item = {
      id: item?.place_id,
      name: item?.name,
      address: item?.formatted_address,
      openNow: item?.opening_hours?.open_now,
      businessStatus: item?.business_status,
      GoogleRating: item?.rating,
    };
    parsed_data.push(parsed_item);
  });
  return parsed_data;
};

// `id: int,
// name: string,
// address: string,
// openNow: bool,
// businessStatus: string,
// GoogleRating: string`;

module.exports = parseRestResult;
