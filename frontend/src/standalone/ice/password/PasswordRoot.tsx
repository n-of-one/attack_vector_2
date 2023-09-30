import React, {Component} from 'react'
import {Reducer, Store} from "redux";
import {configureStore} from "@reduxjs/toolkit";
import {webSocketConnection, WS_NETWORK_APP} from "../../../common/server/WebSocketConnection";
import {Provider} from "react-redux";
import {PasswordContainer} from "./container/PasswordContainer";
import {passwordRootReducer, PasswordRootState} from "./reducer/PasswordRootReducer";
import {passwordIceManager} from "./PasswordIceManager";
import {ICE_INPUT_TERMINAL_ID} from "../../../common/terminal/ActiveTerminalIdReducer";
import {terminalManager} from "../../../common/terminal/TerminalManager";
import {initGenericServerActions} from "../../../hacker/server/GenericServerActionProcessor";
import {ice} from "../IceModel";
import {AuthEnter, AuthStateUpdate, SERVER_AUTH_ENTER, SERVER_AUTH_UPDATE} from "../../app/auth/AuthServerActionProcessor";

interface Props {
    iceId: string,
    nextUrl: string | null
}

export class PasswordRoot extends Component<Props> {

    store: Store

    constructor(props: Props) {
        super(props)
        ice.id = props.iceId

        const preLoadedState = {activeTerminalId: ICE_INPUT_TERMINAL_ID as "iceInput", currentPage: "password"}

        const isDevelopmentServer: boolean = process.env.NODE_ENV === "development"

        this.store = configureStore({
            reducer: passwordRootReducer as Reducer<PasswordRootState>,
            preloadedState: preLoadedState,
            middleware: (getDefaultMiddleware) => [...getDefaultMiddleware()],
            devTools: isDevelopmentServer
        })

        webSocketConnection.create(WS_NETWORK_APP, this.store, () => {
            webSocketConnection.subscribe(`/topic/ice/${ice.id}`)
            webSocketConnection.sendObject("/av/ice/password/enter", {iceId: ice.id, userType: "HACKER"})
        });

        passwordIceManager.init(this.store, props.nextUrl)
        terminalManager.init(this.store)
        initGenericServerActions()
        this.initPasswordIceServerActions()
    }

    render() {
        return (
            <Provider store={this.store}>
                <PasswordContainer/>
            </Provider>
        )
    }

    initPasswordIceServerActions() {
        webSocketConnection.addAction(SERVER_AUTH_ENTER, (data: AuthEnter) => {
            passwordIceManager.enter(data)
        })
        webSocketConnection.addAction(SERVER_AUTH_UPDATE, (data: AuthStateUpdate) => {
            passwordIceManager.serverPasswordIceUpdate(data)
        })
    }
}
