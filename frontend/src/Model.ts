export interface Image {
    id: Number
    fullpath: String
    filename: String
    phototaken: Date
    presetid: Number
    hourTaken: Number
}

export interface Bounds {
    min: Date,
    max: Date
}

export interface Data {

    preview: Array<Image>
    hours: Array<Number>
    bounds: Bounds
}

export interface ImageEntry {
    fullpath: String
    phototaken: Date
}