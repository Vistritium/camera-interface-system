import {Data, Image} from "../Model";
import React, {useEffect, useState} from "react"
import {PresetSelector} from "./PresetSelector";
import {SearchState} from "./SearchState";

type SearchProps = {
    data: Data
}


export const Search = ({data}: SearchProps) => {

    const [selectedPresets, setSelectedPresets] = useState<Array<Image>>();
    const [selectedHours, setSelectedHours] = useState<Array<Number>>();
    const [from, setFrom] = useState<Date>();
    const [to, setTo] = useState<Date>();

    const handleUpdateSelectedPresets = (images: Array<Image>) => {
        setSelectedPresets(images)
    };

    useEffect(() => {
        setSelectedPresets([]);
        setSelectedHours([]);
        setFrom(new Date());
        setTo(new Date());
    }, [data]);

    if (!data) {
        return <div/>
    } else return (
        <div className="container">
            {selectedPresets ? <PresetSelector selectedPresets={selectedPresets} presets={data.preview}
                                               updateSelectedPresets={handleUpdateSelectedPresets}/> : null}
        </div>
    )
};