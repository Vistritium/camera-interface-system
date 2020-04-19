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
    hours: Array<Number>
    bounds: Bounds
    presets: Array<Preset>
}

export interface ImageEntry {
    fullpath: String
    phototaken: Date
    preset: Preset
}

export interface Preset {
    id: Number
    name: String
    displayName?: String
    image: Image
}

export function getName(preset: Preset) {
    return preset.displayName ? preset.displayName : preset.name
}