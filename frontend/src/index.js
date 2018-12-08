import React from 'react';
import {render} from 'react-dom'
import {BrowserRouter, Route, Switch} from 'react-router-dom'
import AppRoot from "./app/AppHome";
import EditorRoot from "./editor/EditorRoot";

render(
    <BrowserRouter>
        <Switch>
            <Route path="/edit/:siteId?" component={EditorRoot} />
            <Route path="/" component={AppRoot} />
        </Switch>
    </BrowserRouter>,
    document.getElementById('root')
);

