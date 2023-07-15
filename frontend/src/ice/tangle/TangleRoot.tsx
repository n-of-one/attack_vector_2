import React, {Component} from 'react'
import {Reducer, Store} from "redux";
import {configureStore} from "@reduxjs/toolkit";
import {webSocketConnection, WS_NETWORK_APP} from "../../common/server/WebSocketConnection";
import {tangleIceManager, TanglePointMoved, TanglePuzzle} from "./TangleIceManager";
import {Provider} from "react-redux";
import {TangleContainer} from "./component/TangleContainer";
import {tangleRootReducer, TangleRootState} from "./reducer/TangleRootReducer";
import {initGenericServerActions} from "../../hacker/server/GenericServerActionProcessor";
import {terminalManager} from "../../common/terminal/TerminalManager";
import {ice} from "../IceModel";
import {SERVER_TANGLE_ENTER, SERVER_TANGLE_POINT_MOVED} from "./reducer/TangleIceReducer";

interface Props {
    iceId: string
    nextUrl: string | null
}
export class TangleRoot extends Component<Props> {

    store: Store

    constructor(props: Props) {
        super(props)
        ice.id = props.iceId
        const preLoadedState = {currentPage: "tangle"}

        const isDevelopmentServer: boolean = process.env.NODE_ENV === "development"

        this.store = configureStore({
            reducer: tangleRootReducer as Reducer<TangleRootState>,
            preloadedState: preLoadedState,
            middleware: (getDefaultMiddleware) =>  [...getDefaultMiddleware()],
            devTools: isDevelopmentServer
        })

        webSocketConnection.create(WS_NETWORK_APP, this.store, () => {
            webSocketConnection.subscribe(`/topic/ice/${props.iceId}`)
            webSocketConnection.sendObject("/av/ice/tangle/enter", {iceId: props.iceId})
        });

        tangleIceManager.init(this.store, props.nextUrl)
        terminalManager.init(this.store)
        initGenericServerActions()
        this.initTangleIceServerActions()
    }

    render() {
        return(
            <Provider store={this.store}>
                <TangleContainer />
            </Provider>
        )
    }

    initTangleIceServerActions() {
        webSocketConnection.addAction(SERVER_TANGLE_ENTER, (data: TanglePuzzle) => {
            tangleIceManager.enter(data)
        })
        webSocketConnection.addAction(SERVER_TANGLE_POINT_MOVED, (data: TanglePointMoved) => {
            tangleIceManager.moved(data)
        })

    }
}


