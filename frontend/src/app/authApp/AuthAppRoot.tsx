import React, {Component} from 'react'
import {Reducer, Store} from "redux";
import {configureStore} from "@reduxjs/toolkit";
import {webSocketConnection, WS_NETWORK_APP} from "../../common/server/WebSocketConnection";
import {Provider} from "react-redux";
import {initGenericServerActions} from "../../hacker/server/GenericServerActionProcessor";
import {terminalManager} from "../../common/terminal/TerminalManager";
import {authAppRootReducer, AuthAppRootState} from "./reducer/AuthAppRootReducer";
import {initAuthAppServerActions} from "./AuthAppServerActionProcessor";
import {AuthAppContainer} from "./component/AuthAppContainer";
import {ice} from "../../ice/IceModel";
import {app} from "../AppId";

interface Props {
    iceId: string
    appId: string
    nextUrl: string
}

export class AuthAppRoot extends Component<Props> {

    store: Store

    constructor(props: Props) {
        super(props)
        ice.id = props.iceId
        app.id = props.appId

        const preLoadedState = {currentPage: "iceApp"}

        const isDevelopmentServer: boolean = process.env.NODE_ENV === "development"

        this.store = configureStore({
            reducer: authAppRootReducer as Reducer<AuthAppRootState>,
            preloadedState: preLoadedState,
            middleware: (getDefaultMiddleware) => [...getDefaultMiddleware()],
            devTools: isDevelopmentServer
        })

        webSocketConnection.create(WS_NETWORK_APP, this.store, () => {
            webSocketConnection.subscribe(`/topic/ice/${ice.id}`)
            webSocketConnection.sendObject("/av/ice/password/enter", {iceId: ice.id, userType: "USER"})
        });

        terminalManager.init(this.store)
        initGenericServerActions()
        initAuthAppServerActions(props.nextUrl)

        document.body.style.backgroundColor = "#333";
    }

    render() {
        return (
            <Provider store={this.store}>
                <AuthAppContainer/>
            </Provider>
        )
    }
}