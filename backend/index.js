const app = require("./server");
const mongoose = require("mongoose");

async function main() {
  mongoose
    .connect("mongodb://localhost:27017/cpen321")
    .then(console.log("Successfully connected to the CPEN 321 database"));

  app.listen(3000, (req, res) => {
    console.log("Server successfully running at port 3000");
  });
}

main().catch((err) => {
  console.log(err);
  mongoose.disconnect();
});
