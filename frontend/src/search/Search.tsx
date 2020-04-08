import {Data, Image} from "../Model";
import React, {useEffect, useState} from "react"
import {PresetSelector} from "./PresetSelector";
import {SearchState} from "./SearchState";
import {HoursSelector} from "./HoursSelector";
import "./Search.css"
import {DatesSelector} from "./DatesSelector";
import moment from "moment-timezone"
import {Panel} from "./Panel";
import {RunGallery} from "../App";

type SearchProps = {
    data: Data
    runGallery: RunGallery
}


export const Search = ({data, runGallery}: SearchProps) => {

    const [selectedPresets, setSelectedPresets] = useState<Array<Image>>();
    const [selectedHours, setSelectedHours] = useState<Array<Number>>();
    const [from, setFrom] = useState<Date>();
    const [to, setTo] = useState<Date>();

    const handleUpdateSelectedPresets = (images: Array<Image>) => {
        setSelectedPresets(images)
    };

    const handleUpdateSelectedHours = (hours: Array<Number>) => {
        setSelectedHours(hours)
    };

    const handleUpdateFromDate = (from: Date) => {
        console.log("setting from: " + from);
        setFrom(from)
    };

    const handleUpdateToDate = (to: Date) => {
        console.log("setting to: " + to);
        setTo(to)
    };

    useEffect(() => {
        setSelectedPresets([]);
        setSelectedHours([]);
        setFrom(moment(data.bounds.max).add(-5, 'days').toDate());
        setTo(data.bounds.max);
    }, [data]);

    if (data && selectedPresets && from && to && selectedHours) {
        return <div className="container">


            <PresetSelector selectedPresets={selectedPresets} presets={data.preview}
                            updateSelectedPresets={handleUpdateSelectedPresets}/>
            < DatesSelector minDate={data.bounds.min} maxDate={data.bounds.max} from={from}
                            to={to} updateFrom={handleUpdateToDate}
                            updateTo={handleUpdateFromDate}/>
            < HoursSelector hours={data.hours} selectedHours={selectedHours}
                            updateSelectedHours={handleUpdateSelectedHours}/>
            < Panel presets={selectedPresets} hours={selectedHours} bounds={{from: from, to: to}} runGallery={runGallery}/>
        </div>
    } else return <div/>
};