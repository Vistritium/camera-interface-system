import React, {useState, useEffect} from 'react';
import {
    BrowserRouter,
    Switch,
    Route,
    Link
} from "react-router-dom";
import App from "./App";
import {Gcode} from "./gcode/Gcode";


export const Router = () => {
    return <BrowserRouter>
        <div>
            <Switch>
                <Route path="/gcode">
                    <Gcode />
                </Route>
                <Route path="/">
                    <App />
                </Route>
            </Switch>
        </div>
    </BrowserRouter>
}