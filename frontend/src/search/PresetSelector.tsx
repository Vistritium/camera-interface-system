import {getName, Image, Preset} from "../Model";
import React from "react"
import "./PresetSelector.scss"
import {imageAddress} from "../Server";
import {SearchState} from "./SearchState";


type PresetSelector = {
    presets: Array<Preset>
    selectedPresets: Array<Preset>
    updateSelectedPresets: (images: Array<Preset>) => void
}

export const PresetSelector = ({presets, updateSelectedPresets, selectedPresets}: PresetSelector) => {
    return (
        <div className="search-input-group">
            <div className="row">
                <div className="preset-selector-image-container">
                    {
                        presets.map((preset, i) => {
                            const selectedClass = (selectedPresets.some(e => e.id === preset.id)) ? "preset-selector-image-selected" : "";

                            const onClick = () => {
                                const newPresets = selectedPresets.some(img => img.id === preset.id) ?
                                    selectedPresets.filter(img => img.id !== preset.id) :
                                    [...selectedPresets, preset]
                                updateSelectedPresets(newPresets)
                            };

                            return (
                                <div key={i} className="preset-selector-preset-image-container">
                                    <div className="preset-selector-preset-container">
                                        <div className="preset-selector-preset">
                                            {getName(preset)}
                                        </div>
                                    </div>
                                    <img src={imageAddress(preset.image)} alt="Loading image"
                                         className={"preset-selector-image " + selectedClass} onClick={onClick}/>
                                </div>
                            )
                        })}
                </div>

            </div>
            <div className="row">
                <div className="selector-button-container">
                    <button type="button" className="btn btn-secondary preset-selector-button"
                            onClick={() => updateSelectedPresets(presets)}>Zaznacz wszystko
                    </button>
                    <button type="button" className="btn btn-secondary preset-selector-button"
                            onClick={() => updateSelectedPresets([])}>Odznacz wszystko
                    </button>
                </div>
            </div>
        </div>
    )
};