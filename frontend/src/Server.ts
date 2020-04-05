import {Image} from "./Model";

const Port = (process.env["NODE_ENV"] === "production") ? window.location.port : process.env["REACT_APP_PORT"];
const Address = `${window.location.protocol}//${window.location.hostname}:${Port}`;

export const Test = process.env["NODE_ENV"];

export function address(s: String) {
    return `${Address}${s}`
}

export function imageAddress(image: Image) {
    return address("/images/download/"+image.fullpath)
}

