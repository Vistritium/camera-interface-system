import React from "react"
import "./GranulationSelector.scss"
import {DateRangePicker} from 'rsuite';
import moment from "moment-timezone";

export type GranulationSelector = {
    granulation: number
    updateGranulation: (images: number) => void
}

export const GranulationSelector = ({granulation, updateGranulation}: GranulationSelector) => {

    const onSelectorChange = (value) => {
        if (value === "0") {
            updateGranulation(0)
        } else if (value === "-1") {
            updateGranulation(-1)
        } else {
            updateGranulation(30)
        }
    }

    return <div className="granulation-selector-container">
        <div>
            <select className="form-control granulation-selector-select"
                    onChange={e => onSelectorChange(e.target.value)}>
                <option value="0">Bez granulacji</option>
                <option value="-1">Pierwsze i ostatnie</option>
                <option value="1">Co n dni</option>
            </select>
        </div>
        <div className="granulation-selector-input">
            {granulation > 1 ?
                <div>
                    <span>Co </span>
                    <input type="number" name="quantity" min="2" defaultValue={granulation} onChange={e => {
                        const value = parseInt(e.target.value);
                        if (value > 1) {
                            updateGranulation(value)
                        }
                    }}
                           onBlur={e => {
                               const value = parseInt(e.target.value)
                               if (value < 2) {
                                   e.target.value = "2"
                               }
                           }}
                    />
                    <span>Dzień</span>
                </div> : null}
        </div>
    </div>
};