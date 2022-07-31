const { getMessaging } = require("firebase-admin/messaging");

const ourGetMessagingAdd = async (message) => {
  return getMessaging()
    .send(message)
    .then((response) => {
      // Response is a message ID string.
      console.log("Successfully sent message:", response);
    })
    .catch((error) => {
      console.log("Error sending message:", error);
    });
};

module.exports = ourGetMessagingAdd;
