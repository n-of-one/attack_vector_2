import React from 'react';
import {render} from 'react-dom'
import {BrowserRouter, Route, Switch} from 'react-router-dom'
import GmRoot from "./gm/GmRoot";
import EditorRoot from "./editor/EditorRoot";
import Login from "./Login";
import RunRoot from "./run/RunRoot";
import HackerRoot from "./hacker/HackerRoot";
import Cookies from "js-cookie";



let ReRoute = (props) => {

    let type = Cookies.get("type");
    if (type === "ADMIN") {
        window.document.location.href = "/gm/admin";
        return
    }
    if (type === "GM") {
        window.document.location.href = "/gm/";
        return
    }
    if (type === "HACKER" || type === "HACKER_MANAGER") {
        window.document.location.href = "/hacker/";
        return
    }
    console.log("Unknown user type: " + type);
    Cookies.remove("jwt");
    Cookies.remove("type");
    Cookies.remove("roles");
    window.document.location.href = "/login"
};

render(
    <BrowserRouter>
        <Switch>
            <Route path="/login" component={Login} />
            {/*<Route path="/hacker/scan/:scanId?" component={ScanRoot} />*/}
            <Route path="/hacker" component={HackerRoot} />
            <Route path="/gm" component={GmRoot} />
            <Route path="/edit/:siteId?" component={EditorRoot} />
            <Route path="/run/:runId?" component={RunRoot} />
            <Route path="/" render={ReRoute} />
        </Switch>
    </BrowserRouter>,
    document.getElementById('root')
);