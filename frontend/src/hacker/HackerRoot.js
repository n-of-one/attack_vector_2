import React, { Component } from 'react'
import { Provider } from 'react-redux'
import RequiresRole from "../common/RequiresRole";
import {applyMiddleware, createStore} from "redux";
import {HACKER_HOME} from "./HackerPages";
import HackerPageChooser from "./HackerPageChooser";
import createSagaMiddleware from 'redux-saga'
import createHackerRootSaga from "./HackerRootSaga";
import hackerRootReducer from "./HackerRootReducer";
import {post} from "../common/RestClient";
import {notify_fatal} from "../common/Notification";
import webSocketConnection from "./WebSocketConnection";
import TerminalManager from "../common/terminal/TerminalManager";
import {RECEIVE_SCANS} from "./home/HomeActions";

class HackerRoot extends Component {


    constructor(props) {
        super(props);
        this.init();
    }

    init() {
        const preLoadedState = {currentPage: HACKER_HOME};
        const sagaMiddleware = createSagaMiddleware();

        this.store = createStore(hackerRootReducer, preLoadedState, applyMiddleware(sagaMiddleware));
        webSocketConnection.create(this.store);

        let scanRootSaga = createHackerRootSaga();
        sagaMiddleware.run(scanRootSaga);

        new TerminalManager(this.store).start();

        post({
            url: "/api/scan/scansOfPlayer",
            body: {},
            ok: (scans) => {
                this.store.dispatch({type: RECEIVE_SCANS, data: scans});
            },
            notok: () => {
                notify_fatal("Failed to retreive scans.");
            }
        });

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
// this.store = createStore(hackerReducer, {currentPage: HACKER_HOME});
// post({
//     url: "/api/scan/scansOfPlayer",
//     body: {},
//     ok: (scans) => {
//         this.store.dispatch({type: RECEIVE_SCANS, data: scans});
//     },
//     notok: () => {
//         notify_fatal("Failed to retreive scans.");
//     }
// });

