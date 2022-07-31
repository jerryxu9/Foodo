const { getMessaging } = require("firebase-admin/messaging");

const ourGetMessagingDelete = async (message) => {
  return getMessaging()
    .send(message)
    .then((response) => {
      // Response is a message ID string.
      console.log("Successfully sent request to delete:", response);
    })
    .catch((error) => {
      console.log("Error sending request to delete:", error);
    });
};

module.exports = ourGetMessagingDelete;
