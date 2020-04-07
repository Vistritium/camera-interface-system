import {Image} from "../Model";
import React from "react"
import "./HoursSelector.scss"
import {imageAddress} from "../Server";
import {SearchState} from "./SearchState";
import {HourSelector} from "./HourSelector";
import {PresetSelector} from "./PresetSelector";

export type HoursSelector = {
    hours: Array<Number>
    selectedHours: Array<Number>
    updateSelectedHours: (images: Array<Number>) => void
}

export const HoursSelector = ({hours, selectedHours, updateSelectedHours}: HoursSelector) => {
    return <div className="search-input-group">
        <div className="hours-selector-container">
            < HourSelector hours={hours} selectedHours={selectedHours} updateSelectedHours={updateSelectedHours}
                           amMode={true}/>
            < HourSelector hours={hours} selectedHours={selectedHours} updateSelectedHours={updateSelectedHours}
                           amMode={false}/>
        </div>
        <div className="row">
            <div className="selector-button-container">
                <button type="button" className="btn btn-secondary preset-selector-button"
                        onClick={() => updateSelectedHours(hours)}>Zaznacz wszystko
                </button>
                <button type="button" className="btn btn-secondary preset-selector-button"
                        onClick={() => updateSelectedHours([])}>Odznacz wszystko
                </button>
            </div>
        </div>
    </div>
};