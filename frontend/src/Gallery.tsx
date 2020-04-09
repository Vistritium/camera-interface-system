import React, {useEffect, useRef, useState} from "react"
import {ImageEntry} from "./Model";
import * as Server from "./Server"
import ImageGallery from 'react-image-gallery';
import "./Gallery.scss"
import moment from "moment-timezone"
import "react-image-gallery/styles/scss/image-gallery.scss";

export type Gallery = {
    images: Array<ImageEntry>
}

export const Gallery = (gallery: Gallery) => {

    const ref = useRef(null)

    useEffect(() => {
        // @ts-ignore
        if (ref && ref.current && ref.current.offsetTop) {
            // @ts-ignore
            window.scrollTo(0, ref.current.offsetTop)
        }
    }, [gallery])

    console.log("gallery: " + JSON.stringify(gallery))
    if (!(gallery.images && gallery.images.length > 0)) {
        return <div/>
    } else {
        const adaptedImages = gallery.images.map(image => ({
            original: Server.imageAddress(image),
            thumbnail: Server.imageAddress(image),
            originalTitle: moment(image.phototaken).format('LLL'),
            thumbnailTitle: moment(image.phototaken).format('LLL')
        }))

        return <div ref={ref} className="image-gallery">
            <ImageGallery items={adaptedImages} lazyLoad={true} slideDuration={0} showIndex={true}
                          startIndex={adaptedImages.length - 1} showPlayButton={false} useTranslate3D={false}/>
        </div>
    }

}