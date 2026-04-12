import React, {Component} from 'react'
import {Reducer, Store} from "redux";
import {configureStore} from "@reduxjs/toolkit";
import {webSocketConnection, WS_UNRESTRICTED} from "../../../common/server/WebSocketConnection";
import {Provider} from "react-redux";
import {initGenericServerActions} from "../../../common/server/GenericServerActionProcessor";
import {StatusLight} from "./StatusLight";
import {defaultState, statusLightReducer, StatusLightState} from "./StatusLightReducers";
import {layer} from "../../StandaloneGlobals";

interface Props {
    layerId: string
}

export class StatusLightRoot extends Component<Props> {

    store: Store

    constructor(props: Props) {
        super(props)
        layer.id = props.layerId
        const preLoadedState = defaultState

        const isDevelopmentServer: boolean = process.env.NODE_ENV === "development"

        this.store = configureStore({
            reducer: statusLightReducer as Reducer<StatusLightState>,
            preloadedState: preLoadedState,
            middleware: (getDefaultMiddleware) => [...getDefaultMiddleware()],
            devTools: isDevelopmentServer
        })

        webSocketConnection.create(WS_UNRESTRICTED, this.store, () => {
            webSocketConnection.subscribe(`/topic/app/${layer.id}`)
            webSocketConnection.sendObject("/app/statusLight/enter", {layerId: layer.id})
        });

        initGenericServerActions({fontSize: 14})
    }

    render() {
        return (
            <Provider store={this.store}>
                <StatusLight/>
            </Provider>
        )
    }
}


