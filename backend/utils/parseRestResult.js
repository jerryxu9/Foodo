// Parse "results" field from Google Place Text Search response
const parseRestResult = (restaurants) => {
  let parsed_data = [];

  restaurants.forEach((item) => {
    let parsed_item = {
      id: item?.place_id,
      name: item?.name,
      address: item?.formatted_address,
      lat: item?.geometry?.location?.lat,
      lng: item?.geometry?.location?.lng,
      openNow: item?.opening_hours?.open_now,
      businessStatus: item?.business_status,
      GoogleRating: item?.rating,
    };
    parsed_data.push(parsed_item);
  });
  return parsed_data;
};

module.exports = parseRestResult;
