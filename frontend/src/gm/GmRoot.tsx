import React, {Component} from 'react'
import { GmHome } from "./component/GmHome"
import {Provider} from 'react-redux'
import {RequiresRole} from "../common/RequiresRole"
import {configureStore} from "@reduxjs/toolkit"
import {GM_SITES, NAVIGATE_PAGE} from "../common/menu/pageReducerX"
import {Reducer, Store} from "redux"
import {gmRootReducer, GmState} from "./GmRootReducer";
import {GmPageChooser} from "./GmPageChooser";
import {WEBSOCKET_RUN, webSocketConnection} from "../common/WebSocketConnection";
import {initGenericServerActions} from "../hacker/server/GenericServerActionProcessor";


interface Props { }

export class GmRoot extends Component<Props>{
    gmStore: Store

    constructor(props: Props) {
        super(props)
        console.log("configure gm store")

        this.gmStore = configureStore({
            reducer: gmRootReducer as Reducer<GmState>
        })

        webSocketConnection.create(WEBSOCKET_RUN, this.gmStore, () => {
            webSocketConnection.send("/av/scan/scansOfPlayer", "")
        })
        initGenericServerActions()

        // set up initial state:
        this.gmStore.dispatch({type: NAVIGATE_PAGE, to: GM_SITES})

        document.body.style.backgroundColor = "#222222"
    }

    render() {
        return (
            <RequiresRole requires="ROLE_SITE_MANAGER">
                <Provider store={this.gmStore}>
                    <GmPageChooser/>
                </Provider>
            </RequiresRole>
        )
    }
}
