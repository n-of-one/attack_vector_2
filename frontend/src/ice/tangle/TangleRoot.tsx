import React, {Component} from 'react'
import {Reducer, Store} from "redux";
import {configureStore} from "@reduxjs/toolkit";
import {WEBSOCKET_ICE, WEBSOCKET_RUN, webSocketConnection} from "../../common/WebSocketConnection";
import {tangleIceManager} from "./component/TangleIceManager";
import {initRunServerActions} from "./TangleServerActionProcessor";
import {RequiresRole} from "../../common/RequiresRole";
import {Provider} from "react-redux";
import {TangleContainer} from "./component/TangleContainer";
import {tangleRootReducer, TangleRootState} from "./TangleRootReducer";

interface Props {
    iceId: string
}
export class TangleRoot extends Component<Props> {

    store: Store

    constructor(props: Props) {
        super(props)
        const preLoadedState = { iceId: props.iceId}

        const isDevelopmentServer: boolean = process.env.NODE_ENV === "development"

        this.store = configureStore({
            reducer: tangleRootReducer as Reducer<TangleRootState>,
            preloadedState: preLoadedState,
            middleware: (getDefaultMiddleware) =>  [...getDefaultMiddleware()],
            devTools: isDevelopmentServer
        })

        webSocketConnection.create(WEBSOCKET_ICE, this.store, () => {
            webSocketConnection.subscribe(`/topic/ice/${props.iceId}`)
            webSocketConnection.sendObject("/av/ice/tangle/enter", {iceId: props.iceId})
        });



        tangleIceManager.init(this.store);

        initRunServerActions(this.store)
    }

    render() {
        return(
            <RequiresRole requires="ROLE_HACKER">
                <Provider store={this.store}>
                    <TangleContainer />
                </Provider>
            </RequiresRole>
        )
    }
}


