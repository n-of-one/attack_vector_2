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
import passwordIceManager from "./run/ice/password/PasswordIceManager";
import tangleIceManager from "./run/ice/tangle/TangleIceManager";
import untangleCanvas from "./run/ice/tangle/TangleIceCanvas";
import {SERVER_START_HACKING_ICE_TANGLE} from "./run/ice/tangle/TangleIceActions";

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
        passwordIceManager.init(this.store);
        tangleIceManager.init(this.store);


        // FIXME
        // const data = {
        //     layerId: "fake",
        //     points: [
        //         {id: 1, x: 250, y: 80},
        //         {id: 2, x: 450, y: 80},
        //         {id: 3, x: 150, y: 280},
        //         {id: 4, x: 650, y: 280},
        //         {id: 5, x: 250, y: 480},
        //         {id: 6, x: 450, y: 480},
        //     ],
        //     lines: [
        //         {id: 1, fromId: 1, toId: 2},
        //         {id: 2, fromId: 1, toId: 3},
        //         {id: 3, fromId: 1, toId: 5},
        //         {id: 5, fromId: 2, toId: 3},
        //         {id: 4, fromId: 2, toId: 5},
        //         {id: 8, fromId: 3, toId: 6},
        //         {id: 9, fromId: 4, toId: 5},
        //         {id: 10, fromId: 4, toId: 6},
        //     ]
        // };
        //
        // setTimeout(() => {
        //     this.store.dispatch({type: SERVER_START_HACKING_ICE_TANGLE, data: data});
        // }, 100);
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

