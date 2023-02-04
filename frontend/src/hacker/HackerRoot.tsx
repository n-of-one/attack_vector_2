import React, {Component} from 'react'
import {Provider} from 'react-redux'
import {RequiresRole} from "../common/RequiresRole";
import {Reducer, Store} from "redux";
import {HackerPageChooser} from "./HackerPageChooser";
import {hackerRootReducer, HackerState} from "./HackerRootReducer";
import {webSocketConnection} from "../common/WebSocketConnection";
import {terminalManager} from "../common/terminal/TerminalManager";
import {passwordIceManager} from "./run/ice/password/PasswordIceManager";
import {tangleIceManager} from "./run/ice/tangle/TangleIceManager";
import {configureStore} from "@reduxjs/toolkit";
import {HACKER_HOME} from "../common/menu/pageReducer";
import {initRunServerActions} from "./server/RunServerActionProcessor";

export class HackerRoot extends Component {

    store: Store

    constructor(props: {}) {
        super(props)
        const preLoadedState = {currentPage: HACKER_HOME};

        const isDevelopmentServer: boolean = process.env.NODE_ENV === "development"

        this.store = configureStore({
            reducer: hackerRootReducer as Reducer<HackerState>,
            preloadedState: preLoadedState,
            middleware: (getDefaultMiddleware) =>  [...getDefaultMiddleware()],
            devTools: isDevelopmentServer
        })

        webSocketConnection.create(this.store, () => {
            webSocketConnection.send("/av/scan/scansOfPlayer", "")
        });

        terminalManager.init(this.store);
        passwordIceManager.init(this.store);
        tangleIceManager.init(this.store);

        initRunServerActions(this.store)
    }

    render() {
        return(
            <RequiresRole requires="ROLE_HACKER">
                <Provider store={this.store}>
                    <HackerPageChooser />
                </Provider>
            </RequiresRole>
        )
    }
}
