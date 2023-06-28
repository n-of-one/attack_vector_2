import React, {Component} from 'react'
import {Reducer, Store} from "redux";
import {configureStore} from "@reduxjs/toolkit";
import {webSocketConnection} from "../../common/server/WebSocketConnection";
import {Provider} from "react-redux";
import {initGenericServerActions} from "../../hacker/server/GenericServerActionProcessor";
import {SwitchRootState, swithRootReducer} from "./SwitchReducers";
import {Switch} from "./Switch";
import {app} from "../AppId";
import {NETWORKED_APP_ENDPOINT} from "../../common/server/ConnectionType";

interface Props {
    appId: string
}

export class SwitchRoot extends Component<Props> {

    store: Store

    constructor(props: Props) {
        app.id = props.appId
        super(props)
        const preLoadedState = {}

        const isDevelopmentServer: boolean = process.env.NODE_ENV === "development"

        this.store = configureStore({
            reducer: swithRootReducer as Reducer<SwitchRootState>,
            preloadedState: preLoadedState,
            middleware: (getDefaultMiddleware) => [...getDefaultMiddleware()],
            devTools: isDevelopmentServer
        })

        webSocketConnection.create(NETWORKED_APP_ENDPOINT, this.store, () => {
            webSocketConnection.subscribe(`/topic/app/${app.id}`)
            webSocketConnection.sendObject("/av/app/statusLight/enter", {appId: app.id})
        });

        initGenericServerActions()
    }

    render() {
        return (
            <Provider store={this.store}>
                <Switch/>
            </Provider>
        )
    }
}


