import React, {useState, useEffect} from 'react';
import logo from './logo.svg';
import './App.scss';
import {Data, Image, ImageEntry} from "./Model";
import * as Server from "./Server"
import {Preview} from "./Preview"
import {Search} from "./search/Search";
import {Gallery} from "./Gallery";
import moment from "moment-timezone"
import 'moment/locale/pl';


export type RunGallery = (images: Array<ImageEntry>) => void

function App() {

    moment.locale("pl");

    const [data, setData] = useState<Data>();
    const [gallery, setGallery] = useState<Gallery>();

    useEffect(() => {
        const fetchData = async () => {
            const preview = await (await fetch(
                Server.address("/api/preview"),
            )).json();
            const hours = await (await fetch(
                Server.address("/api/hours"),
            )).json();
            const bounds = await (await fetch(
                Server.address("/api/bounds"),
            )).json();
            const data: Data = {
                preview: preview,
                hours: hours,
                bounds: {
                    min: new Date(bounds.min),
                    max: new Date(bounds.max)
                }
            };
            console.log(data);
            setData(data);
        };
        fetchData();
    }, []);

    const runGallery: RunGallery = (images => {
        console.log("Running gallery " + JSON.stringify(images))
        const galleryObj: Gallery = {
            images: images
        }
        setGallery(galleryObj)
    })

    if (!data) {
        return <div>
            <h1>Ładowanie</h1>
        </div>
    } else {
        return (
            <div className="App">
                < Preview max={data.bounds.max} images={data?.preview} runGallery={runGallery}/>
                <hr/>
                < Search data={data} runGallery={runGallery}/>
                {gallery ? < Gallery images={gallery.images}/> : null}
            </div>
        );
    }

}

export default App;