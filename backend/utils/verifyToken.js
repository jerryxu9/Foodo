const { OAuth2Client } = require("google-auth-library");
const client = new OAuth2Client(
  "415243569715-ft0h81psvpkm3ufbc9h5qlkomrd1k8bp.apps.googleusercontent.com"
);

async function verify(token) {
  console.log("HI");
  return client
    .verifyIdToken({
      idToken: token,
      audience:
        "415243569715-ft0h81psvpkm3ufbc9h5qlkomrd1k8bp.apps.googleusercontent.com",
    })
    .then((ticket) => {
      const payload = ticket.getPayload();
      const userid = payload["sub"];
      return userid;
    });
}

module.exports = verify;
