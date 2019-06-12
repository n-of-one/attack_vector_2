import React, { Component } from 'react'
import { Provider } from 'react-redux'
import RequiresRole from "../common/RequiresRole";
import {applyMiddleware, compose, createStore} from "redux";
import {HACKER_HOME} from "./HackerPages";
import HackerPageChooser from "./HackerPageChooser";
import createSagaMiddleware from 'redux-saga'
import createHackerRootSaga from "./HackerRootSaga";
import hackerRootReducer from "./HackerRootReducer";
import webSocketConnection from "../common/WebSocketConnection";
import terminalManager from "../common/terminal/TerminalManager";
import {RETRIEVE_USER_SCANS} from "./home/HomeActions";

class HackerRoot extends Component {

    constructor(props) {
        super(props);
        this.init();
    }

    init() {
        const preLoadedState = {currentPage: HACKER_HOME};
        const sagaMiddleware = createSagaMiddleware();

        const composeEnhancers = window.__REDUX_DEVTOOLS_EXTENSION_COMPOSE__ || compose;
        this.store = createStore(hackerRootReducer, preLoadedState, composeEnhancers(applyMiddleware(sagaMiddleware)));
        // this.store = createStore(hackerRootReducer, preLoadedState, applyMiddleware(sagaMiddleware));

        webSocketConnection.create(this.store, () => {
            this.store.dispatch({type: RETRIEVE_USER_SCANS});
        });

        const scanRootSaga = createHackerRootSaga();
        sagaMiddleware.run(scanRootSaga);

        terminalManager.init(this.store);
    }

    renderIfAuthenticated() {
        return (
            <Provider store={this.store}>
                <HackerPageChooser />
            </Provider>
        );
    }

    render() {
        return(
            <RequiresRole requires="ROLE_HACKER">
                {this.renderIfAuthenticated()}
            </RequiresRole>
        )
    }

}

export default HackerRoot

