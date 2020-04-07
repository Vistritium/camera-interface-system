import React from "react"
import 'react-date-range/dist/styles.css';
import 'react-date-range/dist/theme/default.css';
import "./DatesSelector.scss"
import {DateRange, DateRangePicker, Range} from 'react-date-range';
import moment from "moment-timezone"

export type DatesSelector = {
    minDate: Date
    maxDate: Date
    from?: Date,
    to?: Date,
    updateFrom: (images?: Date) => void
    updateTo: (images?: Date) => void
}

export const DatesSelector = ({from, maxDate, minDate, to, updateFrom, updateTo}: DatesSelector) => {
    const selectionRange = {
        startDate: from,
        endDate: to,
        key: 'selection',
    };

    const onSelect: (range: Range) => void = (range: Range) => {
        console.log("on select:" + JSON.stringify(range));
        console.log(range.selection.startDate);
        updateFrom(range.selection.startDate);
        updateTo(range.selection.endDate);
        console.log("on select end" + JSON.stringify(range));
    };

    console.log("from");
    console.log(from);
    console.log("to");
    console.log(to);

    return <div>
        <DateRangePicker
            ranges={[selectionRange]}
/*            minDate={minDate}
            maxDate={maxDate}*/
            onChange={onSelect}
            twoStepChange={true}
        />
    </div>
};