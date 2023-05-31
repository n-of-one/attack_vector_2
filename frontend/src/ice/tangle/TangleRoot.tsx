import React, {Component} from 'react'
import {Reducer, Store} from "redux";
import {configureStore} from "@reduxjs/toolkit";
import {webSocketConnection} from "../../common/server/WebSocketConnection";
import {tangleIceManager} from "./component/TangleIceManager";
import {initTangleIceServerActions} from "./TangleServerActionProcessor";
import {Provider} from "react-redux";
import {TangleContainer} from "./component/TangleContainer";
import {tangleRootReducer, TangleRootState} from "./TangleRootReducer";
import {initGenericServerActions} from "../../hacker/server/GenericServerActionProcessor";
import {terminalManager} from "../../common/terminal/TerminalManager";
import {NETWORKED_APP_ENDPOINT} from "../../common/server/ConnectionType";

interface Props {
    iceId: string
}
export class TangleRoot extends Component<Props> {

    store: Store

    constructor(props: Props) {
        super(props)
        const preLoadedState = { iceId: props.iceId, currentPage: "tangle"}

        const isDevelopmentServer: boolean = process.env.NODE_ENV === "development"

        this.store = configureStore({
            reducer: tangleRootReducer as Reducer<TangleRootState>,
            preloadedState: preLoadedState,
            middleware: (getDefaultMiddleware) =>  [...getDefaultMiddleware()],
            devTools: isDevelopmentServer
        })

        webSocketConnection.create(NETWORKED_APP_ENDPOINT, this.store, () => {
            webSocketConnection.subscribe(`/topic/ice/${props.iceId}`)
            webSocketConnection.sendObject("/av/ice/tangle/enter", {iceId: props.iceId})
        });

        tangleIceManager.init(this.store);
        terminalManager.init(this.store)
        initGenericServerActions()
        initTangleIceServerActions(this.store)
    }

    render() {
        return(
            <Provider store={this.store}>
                <TangleContainer />
            </Provider>
        )
    }
}


