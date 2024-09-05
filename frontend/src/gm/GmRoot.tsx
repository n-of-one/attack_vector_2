import React, {Component} from 'react'
import {Provider} from 'react-redux'
import {RequiresRole} from "../common/user/RequiresRole"
import {configureStore} from "@reduxjs/toolkit"
import {NAVIGATE_PAGE, SITES} from "../common/menu/pageReducer"
import {Reducer, Store} from "redux"
import {gmRootReducer, GmState} from "./GmRootReducer";
import {GmPageChooser} from "./GmPageChooser";
import {webSocketConnection, WS_UNRESTRICTED} from "../common/server/WebSocketConnection";
import {initGenericServerActions} from "../common/server/GenericServerActionProcessor";
import {initGmServerActions} from "./GmServerActionProcessor";


interface Props { }

export class GmRoot extends Component<Props>{
    gmStore: Store

    constructor(props: Props) {
        super(props)

        initGenericServerActions()
        initGmServerActions()

        this.gmStore = configureStore({
            reducer: gmRootReducer as Reducer<GmState>
        })

        webSocketConnection.create(WS_UNRESTRICTED, this.gmStore, () => {
            webSocketConnection.sendWhenReady("/gm/logon", null)
        })

        // set up initial state:
        this.gmStore.dispatch({type: NAVIGATE_PAGE, to: SITES})

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
