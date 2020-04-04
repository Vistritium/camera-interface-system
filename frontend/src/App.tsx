import React from 'react';
import logo from './logo.svg';
import './App.css';
import * as Server from "./Server"

function App() {
    return (
        <div className="App">
            <header className="App-header">
                <h1>Lolz!</h1>
                <h2>{Server.address("/api/preview")}</h2>
                <h3>{Server.Test}</h3>
            </header>
        </div>
    );
}

export default App;
