const Port = (process.env["NODE_ENV"] === "production") ? "" : ":" + process.env["REACT_APP_PORT"];
const Address = window.location.hostname + Port;

export const Test = process.env["NODE_ENV"];

export function address(s: String) {
    return Address + "/" + s
}

