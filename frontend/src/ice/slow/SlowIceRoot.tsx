import React, {Component} from 'react'
import {Reducer, Store} from "redux";
import {configureStore} from "@reduxjs/toolkit";
import {webSocketConnection} from "../../common/WebSocketConnection";
import {RequiresRole} from "../../common/RequiresRole";
import {Provider} from "react-redux";
import {terminalManager} from "../../common/terminal/TerminalManager";
import {slowIceRootReducer, SlowIceRootState} from "./reducer/SlowIceRootReducer";
import {SlowIceContainer} from "./component/SlowIceContainer";
import {slowIceManager} from "./component/SlowIceManager";
import {initGenericServerActions} from "../../hacker/server/GenericServerActionProcessor";
import {initSlowIceServerActions} from "./SlowIceServerActionProcessor";
import {CONNECTION_TYPE_ICE} from "../../common/CurrentUser";

interface Props {
    iceId: string
}

export class SlowIceRoot extends Component<Props> {

    store: Store

    constructor(props: Props) {
        super(props)
        const preLoadedState = { iceId: props.iceId, currentPage: "slowIce"}

        const isDevelopmentServer: boolean = process.env.NODE_ENV === "development"

        this.store = configureStore({
            reducer: slowIceRootReducer as Reducer<SlowIceRootState>,
            preloadedState: preLoadedState,
            middleware: (getDefaultMiddleware) =>  [...getDefaultMiddleware()],
            devTools: isDevelopmentServer
        })

        webSocketConnection.create(CONNECTION_TYPE_ICE, this.store, () => {
            webSocketConnection.subscribe(`/topic/ice/${props.iceId}`)
            webSocketConnection.sendObject("/av/ice/slowIce/enter", {iceId: props.iceId})
        });

        slowIceManager.init(this.store)
        terminalManager.init(this.store)
        initGenericServerActions()
        initSlowIceServerActions(this.store)
    }

    render() {
        return(
            <RequiresRole requires="ROLE_HACKER">
                <Provider store={this.store}>
                    <SlowIceContainer />
                </Provider>
            </RequiresRole>
        )
    }
}


