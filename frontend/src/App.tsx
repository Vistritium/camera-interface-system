import React, {useState, useEffect} from 'react';
import logo from './logo.svg';
import './App.scss';
import {Data, Image} from "./Model";
import * as Server from "./Server"
import {Preview} from "./Preview"
import {Search} from "./search/Search";


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

    if (!data) {
        return <div/>
    } else {
        return (
            <div className="App">
                < Preview images={data?.preview}/>
                <hr/>
                < Search data={data}/>
            </div>
        );
    }


}

export default App;
