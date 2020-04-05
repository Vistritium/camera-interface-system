import {Image} from "../Model";
import React from "react"
import "./PresetSelector.css"
import {imageAddress} from "../Server";
import {SearchState} from "./SearchState";


type PresetSelector = {
    presets: Array<Image>
    selectedPresets: Array<Image>
    updateSelectedPresets: (images: Array<Image>) => void
}

export const PresetSelector = ({presets, updateSelectedPresets, selectedPresets}: PresetSelector) => {
    return (
        <div className="preset-selector-rows">
            <div className="row">
                <div className="preset-selector-image-container">
                    {
                        presets.map((image, i) => {
                            const selectedClass = (selectedPresets.some(e => e.id === image.id)) ? "preset-selector-image-selected" : "";

                            const onClick = () => {
                                const newPresets = selectedPresets.some(img => img.id === image.id) ?
                                    selectedPresets.filter(img => img.id !== image.id) :
                                    [...selectedPresets, image]
                                updateSelectedPresets(newPresets)
                            };

                            return (

                                <img key={i} src={imageAddress(image)} alt="Loading image"
                                     className={"preset-selector-image " + selectedClass} onClick={onClick}/>

                            )
                        })}
                </div>

            </div>
            <div className="row">
                <div className="preset-selector-button-container">
                    <button type="button" className="btn btn-secondary preset-selector-button"
                            onClick={() => updateSelectedPresets(presets)}>Wszystkie
                    </button>
                    <button type="button" className="btn btn-secondary preset-selector-button"
                            onClick={() => updateSelectedPresets([])}>Żaden
                    </button>
                </div>
            </div>
        </div>
    )
};