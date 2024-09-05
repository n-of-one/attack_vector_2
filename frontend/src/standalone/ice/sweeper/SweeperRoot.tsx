import React, {Component} from 'react'
import {Reducer, Store} from "redux";
import {configureStore} from "@reduxjs/toolkit";
import {webSocketConnection, WS_NETWORK_APP} from "../../../common/server/WebSocketConnection";
import {Provider} from "react-redux";
import {sweeperRootReducer, SweeperRootState} from "./reducer/SweeperRootReducer";
import {initGenericServerActions} from "../../../common/server/GenericServerActionProcessor";
import {terminalManager} from "../../../common/terminal/TerminalManager";
import {ice} from "../../StandaloneGlobals";
import {SweeperContainer} from "./component/SweeperContainer";
import {sweeperIceManager} from "./SweeperIceManager";
import {initSweeperServerActions} from "./SweeperServerActionProcessor";

interface Props {
    iceId: string
    externalHack: boolean,
}

export class SweeperRoot extends Component<Props> {

    store: Store

    constructor(props: Props) {
        super(props)
        ice.id = props.iceId
        const preLoadedState = {currentPage: "netwalk"}

        const isDevelopmentServer: boolean = process.env.NODE_ENV === "development"

        this.store = configureStore({
            reducer: sweeperRootReducer as Reducer<SweeperRootState>,
            preloadedState: preLoadedState,
            middleware: (getDefaultMiddleware) => [...getDefaultMiddleware()],
            devTools: isDevelopmentServer
        })

        webSocketConnection.create(WS_NETWORK_APP, this.store, () => {
            webSocketConnection.subscribe(`/topic/ice/${props.iceId}`)
            webSocketConnection.sendObject("/ice/sweeper/enter", {iceId: ice.id})
        });

        sweeperIceManager.init(this.store, props.externalHack);
        terminalManager.init(this.store)
        initGenericServerActions()
        initSweeperServerActions(this.store)
    }

    render() {
        return (
            <Provider store={this.store}>
                <SweeperContainer/>
            </Provider>
        )
    }
}


