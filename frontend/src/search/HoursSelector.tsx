import {Image} from "../Model";
import React from "react"
import "./PresetSelector.css"
import {imageAddress} from "../Server";
import {SearchState} from "./SearchState";

type HoursSelector = {
    hours: Array<Number>
    selectedHours: Array<Number>
    updateSelectedHours: (images: Array<Number>) => void
}

export const HoursSelector = ({hours, selectedHours, updateSelectedHours}: HoursSelector) => {




    return <div/>
};