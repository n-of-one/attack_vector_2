import React, {Component} from 'react'
import {Provider} from 'react-redux'
import {RequiresRole} from "../common/user/RequiresRole";
import {Reducer, Store} from "redux";
import {HackerPageChooser} from "./HackerPageChooser";
import {hackerRootReducer, HackerState} from "./HackerRootReducer";
import {webSocketConnection, WS_HACKER_MAIN} from "../common/server/WebSocketConnection";
import {configureStore} from "@reduxjs/toolkit";
import {HACKER_HOME} from "../common/menu/pageReducer";
import {initRunServerActions} from "./server/RunServerActionProcessor";
import {terminalManager} from "../common/terminal/TerminalManager";

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

        webSocketConnection.create(WS_HACKER_MAIN, this.store, () => {
            webSocketConnection.send("/hacker/logon", "")
        });

        terminalManager.init(this.store)

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
