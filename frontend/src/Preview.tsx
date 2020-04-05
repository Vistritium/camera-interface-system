import React from "react"
import {Image} from "./Model";
import {imageAddress} from "./Server";
import './Preview.css'

type PreviewProps = {
    images: Array<Image>
}
export const Preview = ({images}: PreviewProps) => {

    if (!images) {
        return (<div>loading</div>)
    } else {
        return (

            <div className="preview-image-container">
                {
                    images.map((image, i) => {
                        return (

                            <img key={image.id.toString()} src={imageAddress(image)} alt="Loading image"
                                 className="preview-image"/>

                        )
                    })}
                <div className="preview-image-space-filler"></div>
                <div className="preview-image-space-filler"></div>
                <div className="preview-image-space-filler"></div>
            </div>

        )
    }


}