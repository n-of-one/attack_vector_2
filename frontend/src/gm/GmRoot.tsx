import React, {Component} from 'react'
import {Provider} from 'react-redux'
import {RequiresRole} from "../common/user/RequiresRole"
import {configureStore} from "@reduxjs/toolkit"
import {NAVIGATE_PAGE, Page} from "../common/menu/pageReducer"
import {Reducer, Store} from "redux"
import {gmRootReducer, GmRootState} from "./GmRootReducer";
import {GmPageChooser} from "./GmPageChooser";
import {webSocketConnection, WS_UNRESTRICTED} from "../common/server/WebSocketConnection";
import {initGenericServerActions} from "../common/server/GenericServerActionProcessor";
import {initGmServerActions} from "./GmServerActionProcessor";
import {ROLE_GM} from "../common/user/UserAuthorizations";
import {terminalManager} from "../common/terminal/TerminalManager";


interface Props { }

export class GmRoot extends Component<Props>{
    gmStore: Store

    constructor(props: Props) {
        super(props)

        initGenericServerActions()
        initGmServerActions()

        this.gmStore = configureStore({
            reducer: gmRootReducer as Reducer<GmRootState>
        })

        webSocketConnection.create(WS_UNRESTRICTED, this.gmStore, () => {
            webSocketConnection.sendWhenReady("/gm/logon", null)
        })

        // set up initial state:
        this.gmStore.dispatch({type: NAVIGATE_PAGE, to: Page.SITES})


        terminalManager.init(this.gmStore)
    }

    render() {
        return (
            <RequiresRole requires={ROLE_GM}>
                <Provider store={this.gmStore}>
                    <GmPageChooser/>
                </Provider>
            </RequiresRole>
        )
    }
}
