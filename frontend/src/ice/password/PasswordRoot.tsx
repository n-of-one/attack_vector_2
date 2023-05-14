import React, {Component} from 'react'
import {Reducer, Store} from "redux";
import {configureStore} from "@reduxjs/toolkit";
import {webSocketConnection} from "../../common/WebSocketConnection";
import {RequiresRole} from "../../common/RequiresRole";
import {Provider} from "react-redux";
import {PasswordContainer} from "./container/PasswordContainer";
import {passwordRootReducer, PasswordRootState} from "./PasswordRootReducer";
import {passwordIceManager} from "./container/PasswordIceManager";
import {initPasswordIceServerActions} from "./PasswordServerActionProcessor";
import {ICE_INPUT_TERMINAL_ID} from "../../common/terminal/ActiveTerminalIdReducer";
import {terminalManager} from "../../common/terminal/TerminalManager";
import {initGenericServerActions} from "../../hacker/server/GenericServerActionProcessor";
import {CONNECTION_TYPE_ICE} from "../../common/CurrentUser";

interface Props {
    iceId: string
}

export class PasswordRoot extends Component<Props> {

    store: Store

    constructor(props: Props) {
        super(props)
        const preLoadedState = {iceId: props.iceId, activeTerminalId: ICE_INPUT_TERMINAL_ID, currentPage: "password"}

        const isDevelopmentServer: boolean = process.env.NODE_ENV === "development"

        this.store = configureStore({
            reducer: passwordRootReducer as Reducer<PasswordRootState>,
            preloadedState: preLoadedState,
            middleware: (getDefaultMiddleware) => [...getDefaultMiddleware()],
            devTools: isDevelopmentServer
        })

        webSocketConnection.create(CONNECTION_TYPE_ICE, this.store, () => {
            webSocketConnection.subscribe(`/topic/ice/${props.iceId}`)
            webSocketConnection.sendObject("/av/ice/password/enter", {iceId: props.iceId})
        });

        passwordIceManager.init(this.store)
        terminalManager.init(this.store)
        initGenericServerActions()
        initPasswordIceServerActions(this.store)
    }

    render() {
        return (
            <RequiresRole requires="ROLE_HACKER">
                <Provider store={this.store}>
                    <PasswordContainer/>
                </Provider>
            </RequiresRole>
        )
    }
}


