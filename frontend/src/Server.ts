import {Image, ImageEntry, Preset} from "./Model";
import moment from "moment-timezone";

const Port = (process.env["NODE_ENV"] === "production") ? window.location.port : process.env["REACT_APP_PORT"];
const Address = `${window.location.protocol}//${window.location.hostname}:${Port}`;

export const Test = process.env["NODE_ENV"];

export function address(s: String) {
    return `${Address}${s}`
}

export interface HasFullpath {
    fullpath: String
}

export function imageAddress(image: HasFullpath) {
    return address("/images/download/" + image.fullpath)
}

export type ImageUrlFetchType = "count" | "images"
export const imageUrl = (type: ImageUrlFetchType, from: Date, to: Date, hours: Array<Number>, presets: Array<Preset>) => {
    const toPlusDay = moment(to).add(1, 'days')
    return address("/api/images") + "?" + new URLSearchParams({
        "min": from.toISOString(),
        "max": toPlusDay.toISOString(),
        "hours": hours.join(","),
        "presets": presets.map(p => p.id).join(","),
        "count": type == "count" ? "true" : "false"
    });
}

export const fetchImages = async (from: Date, to: Date, hours: Array<Number>, presets: Array<Preset>) => {
    const response = await fetch(imageUrl("images", from, to, hours, presets))
    const json = await response.json()
    // @ts-ignore
    const data: Array<ImageEntry> = json.map(e => ({
        fullpath: e.fullpath,
        phototaken: new Date(e.phototaken),
        preset: presets.find(preset => preset.id === e.presetId)
    }))
    return data;
}

