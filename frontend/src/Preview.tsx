import React from "react"
import {Image} from "./Model";
import './Preview.scss'
import {RunGallery} from "./App";
import * as Server from "./Server"
import moment from "moment-timezone"

type PreviewProps = {
    images: Array<Image>,
    runGallery: RunGallery,
    max: Date
}
export const Preview = ({images, runGallery, max}: PreviewProps) => {

    const onClick = (image: Image) => {
        const from = moment(max).add(-14, 'days')
        const execute = async () => {
            const data = await Server.fetchImages(from.toDate(), max, [16], [image])
            runGallery(data)
        }
        execute()
    }

    if (!images) {
        return (<div>loading</div>)
    } else {
        return (

            <div className="preview-image-container">
                {
                    images.map((image, i) => {
                        return (
                            <img key={image.id.toString()} src={Server.imageAddress(image)} alt="Loading image"
                                 className="preview-image" onClick={() => onClick(image)}/>

                        )
                    })}
                <div className="preview-image-space-filler"></div>
                <div className="preview-image-space-filler"></div>
                <div className="preview-image-space-filler"></div>
            </div>

        )
    }


}