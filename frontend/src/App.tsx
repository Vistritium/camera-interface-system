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
            const previewLoad = fetch(Server.address("/api/preview")).then(s => s.json())
            const hoursLoad = fetch(Server.address("/api/hours")).then(s => s.json())
            const boundsLoad = fetch(Server.address("/api/bounds")).then(s => s.json())
            const presetsLoad = fetch(Server.address("/api/presets")).then(s => s.json())

            const preview = await (await previewLoad)
            const hours = await (await hoursLoad)
            const bounds = await (await boundsLoad)
            const presets = await (await presetsLoad)
            console.log(preview)
            const data: Data = {
                hours: hours,
                bounds: {
                    min: new Date(bounds.min),
                    max: new Date(bounds.max)
                },
                presets: presets.map(p => {
                    return {
                        ...p,
                        image: preview.find(prev => prev.presetid === p.id) as Image
                    }
                })
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
                < Preview max={data.bounds.max} runGallery={runGallery} presets={data.presets}/>
                <hr/>
                < Search data={data} runGallery={runGallery}/>
                {gallery ? < Gallery images={gallery.images}/> : null}
            </div>
        );
    }

}

export default App;