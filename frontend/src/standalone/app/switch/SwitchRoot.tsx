import React, {Component} from 'react'
import {Reducer, Store} from "redux";
import {configureStore} from "@reduxjs/toolkit";
import {webSocketConnection, WS_NETWORK_APP} from "../../../common/server/WebSocketConnection";
import {Provider} from "react-redux";
import {initGenericServerActions} from "../../../common/server/GenericServerActionProcessor";
import {SwitchRootState, swithRootReducer} from "./SwitchReducers";
import {Switch} from "./Switch";
import {layer} from "../../StandaloneGlobals";

interface Props {
    layerId: string
}

export class SwitchRoot extends Component<Props> {

    store: Store

    constructor(props: Props) {
        super(props)
        layer.id = props.layerId
        const preLoadedState = {}

        const isDevelopmentServer: boolean = process.env.NODE_ENV === "development"

        this.store = configureStore({
            reducer: swithRootReducer as Reducer<SwitchRootState>,
            preloadedState: preLoadedState,
            middleware: (getDefaultMiddleware) => [...getDefaultMiddleware()],
            devTools: isDevelopmentServer
        })

        webSocketConnection.create(WS_NETWORK_APP, this.store, () => {
            webSocketConnection.subscribe(`/topic/app/${layer.id}`)
            webSocketConnection.sendObject("/app/statusLight/enter", {layerId: layer.id})
        });

        initGenericServerActions({fontSize: 14})
    }

    render() {
        return (
            <Provider store={this.store}>
                <Switch/>
            </Provider>
        )
    }
}


