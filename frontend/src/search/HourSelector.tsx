import {Image} from "../Model";
import React from "react"
import "./PresetSelector.scss"
import {imageAddress} from "../Server";
import {SearchState} from "./SearchState";
import {HoursSelector} from "./HoursSelector";
// @ts-ignore
import PieMenu, {Slice} from 'react-pie-menu';
import * as _ from "lodash";

type HourSelector = HoursSelector & { amMode: Boolean }

export const HourSelector = ({hours, selectedHours, updateSelectedHours, amMode}: HourSelector) => {

    const getHour = (i: number) => {
        const amHour = (((i + 12) - 1) % 12) + 1;
        if (amMode) {
            if (amHour === 12) {
                return 0
            } else {
                return amHour
            }
        } else {
            const pmHour = amHour + 12
            if (pmHour === 24) {
                return 12
            } else {
                return pmHour
            }
        }
    };

    return <div className="hour-selector-container">
        <PieMenu
            radius='125px'
            centerRadius='30px'
            centerX={0}
            centerY={0}
        >
            {_.times(12).map(i => {
                const hour = getHour(i)

                const isExisting = hours.some(i => i === hour);
                const isSelected = selectedHours.some(i => i === hour);

                const onSelect = () => {
                    const newHours = (selectedHours.some(h => h === hour)) ?
                        selectedHours.filter(h => h !== hour) :
                        [...selectedHours, hour];
                    updateSelectedHours(newHours)
                };

                const selection = () => {
                    if (!isExisting) return "disabled";
                    else if (isSelected) return "selected";
                    else return "not_selected";
                };
                const isSecond = () => ((i % 2) === 0).toString();

                /* attrs={isExisting ? {} : {enabled: "false"}}> */
                const sliceClass = `hour-selector-slice ${selectedHours.some(i => i === hour) ? "hour-selector-slice-selected " : ""}${isExisting ? "" : "hour-selector-slice-not-existing"}`
                const isSelectedStr = isSelected.toString();

                return <Slice key={i} onSelect={onSelect} attrs={{selection: selection(), is_second: isSecond()}}>
                    <div
                        /*  + selectedHours.some(i => i === hour) ? "hour-selector-elem-selected " : "" + hours.some(i => i === hour) ? "hour-selector-elem-not-existing" : "" */
                        className={"hour-selector-elem " +
                        (isSelected ? "hour-selector-elem-selected " : "") +
                        (isExisting ? "" : "hour-selector-elem-not-existing")}
                        key={i}>{hour}
                    </div>
                </Slice>
            })}
        </PieMenu>
    </div>
};