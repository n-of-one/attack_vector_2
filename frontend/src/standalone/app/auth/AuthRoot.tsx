import React, {Component} from 'react'
import {Reducer, Store} from "redux";
import {configureStore} from "@reduxjs/toolkit";
import {webSocketConnection, WS_NETWORK_APP} from "../../../common/server/WebSocketConnection";
import {Provider} from "react-redux";
import {initGenericServerActions} from "../../../hacker/server/GenericServerActionProcessor";
import {terminalManager} from "../../../common/terminal/TerminalManager";
import {AuthAppRootState, authRootReducer} from "./reducer/AuthRootReducer";
import {initAuthServerActions} from "./AuthServerActionProcessor";
import {AuthContainer} from "./component/AuthContainer";
import {ice, layer} from "../../StandaloneGlobals";

interface Props {
    iceId: string
    layerId: string
}

export class AuthRoot extends Component<Props> {

    store: Store

    constructor(props: Props) {
        super(props)
        ice.id = props.iceId
        layer.id = props.layerId

        const preLoadedState = {currentPage: "iceApp"}

        const isDevelopmentServer: boolean = process.env.NODE_ENV === "development"

        this.store = configureStore({
            reducer: authRootReducer as Reducer<AuthAppRootState>,
            preloadedState: preLoadedState,
            middleware: (getDefaultMiddleware) => [...getDefaultMiddleware()],
            devTools: isDevelopmentServer
        })

        webSocketConnection.create(WS_NETWORK_APP, this.store, () => {
            webSocketConnection.subscribe(`/topic/ice/${ice.id}`)
            webSocketConnection.sendObject("/ice/password/enter", {iceId: ice.id})
        });

        terminalManager.init(this.store)
        initGenericServerActions()
        initAuthServerActions()

        document.body.style.backgroundColor = "#333";
    }

    render() {
        return (
            <Provider store={this.store}>
                <AuthContainer/>
            </Provider>
        )
    }
}
