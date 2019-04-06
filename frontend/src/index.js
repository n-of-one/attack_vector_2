import React from 'react';
import {render} from 'react-dom'
import {BrowserRouter, Route, Switch} from 'react-router-dom'
import AppRoot from "./app/AppHome";
import EditorReduxRoot from "./editor/EditorReduxRoot";
import Login from "./app/auth/component/Login";

render(
    <BrowserRouter>
        <Switch>
            <Route path="/login" component={Login} />
            <Route path="/edit/:siteId?" component={EditorReduxRoot} />
            <Route path="/" component={AppRoot} />
        </Switch>
    </BrowserRouter>,
    document.getElementById('root')
);

