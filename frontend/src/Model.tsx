export interface Image {
    id: Number
    fullpath: String
    filename: String
    phototaken: Date
    presetid: Number
    hourTaken: Number
}


export interface Data {

    preview: Array<Image>

}