const fs = require("fs")

if (fs.existsSync("tmp_build")) {
    fs.rmdirSync("tmp_build", {recursive: true})
}
fs.renameSync("build", "tmp_build")
fs.mkdirSync("build")
fs.renameSync("tmp_build", "build/frontend")
