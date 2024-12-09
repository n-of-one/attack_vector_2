import React, {Component} from 'react'
import {Reducer, Store} from "redux";
import {hackerRootReducer, HackerRootState} from "./HackerRootReducer";
import {webSocketConnection, WS_HACKER_MAIN} from "../common/server/WebSocketConnection";
import {configureStore} from "@reduxjs/toolkit";
import {initRunServerActions} from "./RunServerActionProcessor";
import {terminalManager} from "../common/terminal/TerminalManager";
import {initGenericServerActions} from "../common/server/GenericServerActionProcessor";
import {Page} from "../common/menu/pageReducer";
import {Script, ScriptState} from "../common/script/ScriptModel";
import {RequiresRole} from "../common/user/RequiresRole";
import {Provider} from "react-redux";
import {HackerPageChooser} from "./HackerPageChooser";

export class HackerRoot extends Component {

    store: Store

    constructor(props: {}) {
        super(props)
        const preLoadedState = {currentPage: Page.HACKER_HOME};

        const isDevelopmentServer: boolean = process.env.NODE_ENV === "development"

        this.store = configureStore({
            reducer: hackerRootReducer as Reducer<HackerRootState>,
            preloadedState: preLoadedState,
            middleware: (getDefaultMiddleware) =>  [...getDefaultMiddleware()],
            devTools: isDevelopmentServer
        })

        initGenericServerActions()
        initRunServerActions(this.store)

        webSocketConnection.create(WS_HACKER_MAIN, this.store, () => {
            webSocketConnection.send("/hacker/logon", null)
        });

        terminalManager.init(this.store)

    }


    render() {

        const scripts: Script[] = []


        for (var i = 0; i < 100; i++) {
            scripts.push({
                id: i.toString(),
                effects: ["a", "b"],
                name: "Script " + i,
                ram: 10,
                state: ScriptState.USED,
                code: "code",
                timeLeft: "1h",
                loaded: false,
                loadStartedAt: "2024-12-01T15:38:40.9179757+02:00",
                loadTimeFinishAt: "2024-12-01T16:08:40.9179757+02:00",

            })
        }

        return(
            <RequiresRole requires="ROLE_HACKER">
                <Provider store={this.store}>
                    <HackerPageChooser />
                </Provider>
            </RequiresRole>
        )
    }
}
