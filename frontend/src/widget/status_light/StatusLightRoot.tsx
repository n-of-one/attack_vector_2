import React, {Component} from 'react'
import {Reducer, Store} from "redux";
import {configureStore} from "@reduxjs/toolkit";
import {webSocketConnection} from "../../common/server/WebSocketConnection";
import {Provider} from "react-redux";
import {initGenericServerActions} from "../../hacker/server/GenericServerActionProcessor";
import {StatusLight} from "./StatusLight";
import {defaultState, statusLightReducer, StatusLightState} from "./StatusLightReducers";
import {WS_UNRESTRICTED} from "../../common/server/ConnectionType";
import {app} from "../../app/AppId";

interface Props {
}

export class StatusLightRoot extends Component<Props> {

    store: Store

    constructor(props: Props) {
        super(props)
        const preLoadedState = defaultState

        const isDevelopmentServer: boolean = process.env.NODE_ENV === "development"

        this.store = configureStore({
            reducer: statusLightReducer as Reducer<StatusLightState>,
            preloadedState: preLoadedState,
            middleware: (getDefaultMiddleware) => [...getDefaultMiddleware()],
            devTools: isDevelopmentServer
        })

        webSocketConnection.create(WS_UNRESTRICTED, this.store, () => {
            webSocketConnection.subscribe(`/topic/app/${app.id}`)
            webSocketConnection.sendObject("/av/app/statusLight/enter", {appId: app.id})
        });

        initGenericServerActions()
    }

    render() {
        return (
            <Provider store={this.store}>
                <StatusLight/>
            </Provider>
        )
    }
}


