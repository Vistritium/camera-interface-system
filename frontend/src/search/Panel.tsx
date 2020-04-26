import {Image, ImageEntry, Preset} from "../Model";
import React, {useEffect, useState} from "react"
import "./Panel.scss"
import * as Server from "../Server"
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faImages} from '@fortawesome/free-solid-svg-icons'
import moment from "moment-timezone"
import {RunGallery} from "../App";

type Panel = {
    presets: Array<Preset>
    hours: Array<Number>
    bounds: {
        from: Date
        to: Date
    },
    runGallery: RunGallery,
    granulation: number
}

export const Panel = ({bounds: {from, to}, hours, presets, runGallery, granulation}: Panel) => {

    const [count, setCount] = useState<Number | undefined>();


    useEffect(() => {
        const fetchData = async () => {
            if (presets.length > 0 && hours.length > 0) {
                const data = await fetch(Server.imageUrl("count", from, to, hours, presets, granulation))
                if (data.ok) {
                    const text = await data.text()
                    setCount(parseInt(text))
                }
            }
        }
        fetchData()
    }, [from, to, hours, presets, granulation]);

    const onClick = () => {
        const execute = async () => {
            const data = await Server.fetchImages(from, to, hours, presets, granulation)
            runGallery(data)
        }
        execute()
    }

    if (presets.length === 0 || hours.length === 0 || (count == undefined)) {
        return <div/>
    } else {
        return <div className="search-panel">
            <div className="search-pannel-inner" onClick={onClick}>
                <div className="search-pannel-inner-icon"><FontAwesomeIcon icon={faImages}/></div>
                <div className="search-pannel-inner-text">{count}</div>
            </div>

        </div>
    }
}