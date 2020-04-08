import React from "react"
import "./DatesSelector.scss"
import {DateRangePicker} from 'rsuite';

export type DatesSelector = {
    minDate: Date
    maxDate: Date
    from: Date,
    to: Date,
    updateFrom: (images: Date) => void
    updateTo: (images: Date) => void
}

export const DatesSelector = ({from, maxDate, minDate, to, updateFrom, updateTo}: DatesSelector) => {

    const onChange = (dates: Array<Date>) => {
        updateFrom(dates[1]);
        updateTo(dates[0]);
    };

    return <div className="date-selector-container">
        <DateRangePicker disabledDate={DateRangePicker.allowedRange(minDate, maxDate)} onChange={onChange} defaultValue={[from, to]}  />
    </div>
};