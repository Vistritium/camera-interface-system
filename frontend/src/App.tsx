import React, {useState, useEffect} from 'react';
import logo from './logo.svg';
import './App.scss';
import {Data, Image, ImageEntry} from "./Model";
import * as Server from "./Server"
import {Preview} from "./Preview"
import {Search} from "./search/Search";


export type RunGallery = (images: Array<ImageEntry>) => void

function App() {

    const [data, setData] = useState<Data>();

    useEffect(() => {
        const fetchData = async () => {
            const preview = await (await fetch<>(
                Server.address("/api/preview"),
            )).json();
            const hours = await (await fetch<>(
                Server.address("/api/hours"),
            )).json();
            const bounds = await (await fetch<>(
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
        console.log("Running gallery" + JSON.stringify(images))
    })

    if (!data) {
        return <div>
            <h1>Loading</h1>
        </div>
    } else {
        return (
            <div className="App">
                < Preview images={data?.preview}/>
                <hr/>
                < Search data={data} runGallery={runGallery}/>
            </div>
        );
    }

}

export default App;

