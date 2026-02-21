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
import {initGenericServerActions} from "../../../common/server/GenericServerActionProcessor";
import {ice} from "../../StandaloneGlobals";
import {Page} from "../../../common/menu/pageReducer";
import {initPasswordIceServerActions} from "./PasswordServerActionProcessor";

interface Props {
    iceId: string,
    externalHack: boolean,
}

export class PasswordRoot extends Component<Props> {

    store: Store

    constructor(props: Props) {
        super(props)
        ice.id = props.iceId

        const preLoadedState = {activeTerminalId: ICE_INPUT_TERMINAL_ID as "iceInput", currentPage: Page.ICE}

        const isDevelopmentServer: boolean = process.env.NODE_ENV === "development"

        this.store = configureStore({
            reducer: passwordRootReducer as Reducer<PasswordRootState>,
            preloadedState: preLoadedState,
            middleware: (getDefaultMiddleware) => [...getDefaultMiddleware()],
            devTools: isDevelopmentServer
        })

        webSocketConnection.create(WS_NETWORK_APP, this.store, () => {
            webSocketConnection.subscribe(`/topic/ice/${ice.id}`)
            webSocketConnection.sendObject("/ice/password/enter", {iceId: ice.id })
        });

        passwordIceManager.init(this.store, props.externalHack)
        terminalManager.init(this.store)
        initGenericServerActions({fontSize: 14})
        initPasswordIceServerActions()
    }

    render() {
        return (
            <Provider store={this.store}>
                <PasswordContainer/>
            </Provider>
        )
    }


}
