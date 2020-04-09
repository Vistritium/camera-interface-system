import React from "react"
import "./DatesSelector.scss"
import {DateRangePicker} from 'rsuite';
import moment from "moment-timezone";

export type DatesSelector = {
    minDate: Date
    maxDate: Date
    from: Date,
    to: Date,
    updateFrom: (images: Date) => void
    updateTo: (images: Date) => void
}

const Locale = {
    sunday: 'Nie',
    monday: 'Pon',
    tuesday: 'Wto',
    wednesday: 'Śro',
    thursday: 'Czw',
    friday: 'Pią',
    saturday: 'Sob',
    ok: 'OK',
    today: 'Dziś',
    yesterday: 'Wczoraj',
    hours: 'Godzin',
    minutes: 'Minut',
    seconds: 'Sekund',
    last7Days: "Ostatnie 7 dni"
};

export const DatesSelector = ({from, maxDate, minDate, to, updateFrom, updateTo}: DatesSelector) => {

    const onChange = (dates: Array<Date>) => {
        updateFrom(dates[1]);
        updateTo(dates[0]);
    };


    const maxDateUpdated = moment(maxDate).add(1, 'hours').toDate()
    const toDefault = maxDate === to ? maxDateUpdated : to


    return <div className="date-selector-container">
        <DateRangePicker disabledDate={DateRangePicker.allowedRange(minDate, maxDateUpdated)}
            // @ts-ignore
                         onChange={onChange}
                         defaultValue={[from, toDefault]} locale={Locale} isoWeek={true}/>
    </div>
};