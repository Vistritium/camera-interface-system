import React from "react"
import {Image, Preset} from "./Model";
import './Preview.scss'
import {RunGallery} from "./App";
import * as Server from "./Server"
import moment from "moment-timezone"

type PreviewProps = {
    presets: Array<Preset>
    runGallery: RunGallery,
    max: Date
}
export const Preview = ({runGallery, max, presets}: PreviewProps) => {

    const onClick = (image: Image) => {
        const from = moment(max).add(-14, 'days')
        const execute = async () => {
            const preset: Preset = presets.find(p => p.id === image.presetid) as Preset
            const data = await Server.fetchImages(from.toDate(), max, [16], [preset])
            runGallery(data)
        }
        execute()
    }

    if (!presets) {
        return (<div>loading</div>)
    } else {
        return (

            <div className="preview-image-container">
                {
                    presets.map((preset, i) => {
                        return (
                            <img key={preset.id.toString()} src={Server.imageAddress(preset.image)} alt="Loading image"
                                 className="preview-image" onClick={() => onClick(preset.image)}/>

                        )
                    })}
                <div className="preview-image-space-filler"></div>
                <div className="preview-image-space-filler"></div>
                <div className="preview-image-space-filler"></div>
            </div>

        )
    }


}