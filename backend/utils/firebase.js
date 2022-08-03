var { initializeApp } = require("firebase-admin/app");
var admin = require("firebase-admin");
var serviceAccount = require("../foodo-354901-firebase-adminsdk-7bh3t-c3774a88a2");

const app = initializeApp({
  credential: admin.credential.cert(serviceAccount),
});

exports.app = app;
