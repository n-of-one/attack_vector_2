import React, {Component} from 'react'
import {Reducer, Store} from "redux";
import {configureStore} from "@reduxjs/toolkit";
import {webSocketConnection, WS_NETWORK_APP} from "../../common/server/WebSocketConnection";
import {Provider} from "react-redux";
import {PasswordContainer} from "./container/PasswordContainer";
import {passwordRootReducer, PasswordRootState} from "./reducer/PasswordRootReducer";
import {passwordIceManager} from "./container/PasswordIceManager";
import {initPasswordIceServerActions} from "./PasswordServerActionProcessor";
import {ICE_INPUT_TERMINAL_ID} from "../../common/terminal/ActiveTerminalIdReducer";
import {terminalManager} from "../../common/terminal/TerminalManager";
import {initGenericServerActions} from "../../hacker/server/GenericServerActionProcessor";
import {ice} from "../IceModel";

interface Props {
    iceId: string
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

        passwordIceManager.init(this.store)
        terminalManager.init(this.store)
        initGenericServerActions()
        initPasswordIceServerActions(this.store)
    }

    render() {
        return (
            <Provider store={this.store}>
                <PasswordContainer/>
            </Provider>
        )
    }
}

// export class PasswordRootSimplified extends Component<Props> {
//
//
//     constructor(props: Props) {
//         super(props)
//         const preLoadedState = {activeTerminalId: ICE_INPUT_TERMINAL_ID, currentPage: "password"}
//
//         const store = comynfigureStore(passwordRootReducer, preLoadedState )
//
//
//         passwordIceManager.init(store)
//         terminalManager.init(store)
//         initPasswordIceServerActions(store)
//
//         webSocketConnection.sendObject("/av/ice/password/enter", {iceId: props.iceId})
//     }
//
//     render() {
//         return <PasswordContainer/>
//     }
// }

