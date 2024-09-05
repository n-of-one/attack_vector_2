import React, {Component} from 'react'
import {Reducer, Store} from "redux";
import {configureStore} from "@reduxjs/toolkit";
import {webSocketConnection, WS_NETWORK_APP} from "../../../common/server/WebSocketConnection";
import {Provider} from "react-redux";
import {terminalManager} from "../../../common/terminal/TerminalManager";
import {tarRootReducer, TarRootState} from "./reducer/TarRootReducer";
import {TarContainer} from "./component/TarContainer";
import {tarManager} from "./component/TarManager";
import {initGenericServerActions} from "../../../common/server/GenericServerActionProcessor";
import {initTarServerActions} from "./TarServerActionProcessor";
import {ice} from "../../StandaloneGlobals";

interface Props {
    iceId: string
    externalHack: boolean,
}

export class TarRoot extends Component<Props> {

    store: Store

    constructor(props: Props) {
        super(props)
        ice.id = props.iceId
        const preLoadedState = {currentPage: "tar"}

        const isDevelopmentServer: boolean = process.env.NODE_ENV === "development"

        this.store = configureStore({
            reducer: tarRootReducer as Reducer<TarRootState>,
            preloadedState: preLoadedState,
            middleware: (getDefaultMiddleware) => [...getDefaultMiddleware()],
            devTools: isDevelopmentServer
        })

        webSocketConnection.create(WS_NETWORK_APP, this.store, () => {
            webSocketConnection.subscribe(`/topic/ice/${props.iceId}`)
            webSocketConnection.sendObject("/ice/tar/enter", {iceId: props.iceId})
        });

        tarManager.init(this.store, props.externalHack)
        terminalManager.init(this.store)
        initGenericServerActions()
        initTarServerActions(this.store)
    }

    render() {
        return (
            <Provider store={this.store}>
                <TarContainer/>
            </Provider>
        )
    }
}


