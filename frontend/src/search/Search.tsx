import {Data, Image, Preset} from "../Model";
import React, {useEffect, useState} from "react"
import {PresetSelector} from "./PresetSelector";
import {SearchState} from "./SearchState";
import {HoursSelector} from "./HoursSelector";
import "./Search.scss"
import {DatesSelector} from "./DatesSelector";
import moment from "moment-timezone"
import {Panel} from "./Panel";
import {RunGallery} from "../App";
import {GranulationSelector} from "./GranulationSelector";

type SearchProps = {
    data: Data
    runGallery: RunGallery
}


export const Search = ({data, runGallery}: SearchProps) => {

    const [selectedPresets, setSelectedPresets] = useState<Array<Preset>>();
    const [selectedHours, setSelectedHours] = useState<Array<Number>>();
    const [from, setFrom] = useState<Date>();
    const [to, setTo] = useState<Date>();
    const [granulation, setGranulation] = useState<number>(0);

    const handleUpdateSelectedPresets = (images: Array<Preset>) => {
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

    const handleUpdateGranulation = (granulation: number) => {
        setGranulation(granulation)
    }

    useEffect(() => {
        setSelectedPresets([]);
        setSelectedHours([9, 10, 11, 12, 13, 14, 15, 16, 17]);
        setFrom(moment(data.bounds.max).add(-1, 'days').toDate());
        setTo(data.bounds.max);
    }, [data]);

    if (data && selectedPresets && from && to && selectedHours) {
        return <div className="container">

            <PresetSelector selectedPresets={selectedPresets} presets={data.presets}
                            updateSelectedPresets={handleUpdateSelectedPresets}/>
            <GranulationSelector granulation={granulation} updateGranulation={handleUpdateGranulation}/>
            < DatesSelector minDate={data.bounds.min} maxDate={data.bounds.max} from={from}
                            to={to} updateFrom={handleUpdateToDate}
                            updateTo={handleUpdateFromDate}/>
            < HoursSelector hours={data.hours} selectedHours={selectedHours}
                            updateSelectedHours={handleUpdateSelectedHours}/>
            < Panel presets={selectedPresets} hours={selectedHours} bounds={{from: from, to: to}}
                    runGallery={runGallery} granulation={granulation} />
        </div>
    } else return <div/>
};