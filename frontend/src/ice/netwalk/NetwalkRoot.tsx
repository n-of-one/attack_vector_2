import React, {Component} from 'react'
import {Reducer, Store} from "redux";
import {configureStore} from "@reduxjs/toolkit";
import {webSocketConnection} from "../../common/WebSocketConnection";
import {Provider} from "react-redux";
import {NetwalkContainer} from "./component/NetwalkContainer";
import {NetwalkRootState, netwalkRootReducer} from "./reducer/NetwalkRootReducer";
import {initGenericServerActions} from "../../hacker/server/GenericServerActionProcessor";
import {terminalManager} from "../../common/terminal/TerminalManager";
import {initNetwalkServerActions} from "./NetwalkServerActionProcessor";
import {netwalkManager} from "./component/NetwalkManager";
import {CONNECTION_TYPE_ICE} from "../../common/CurrentUser";

interface Props {
    iceId: string
}

export class NetwalkRoot extends Component<Props> {

    store: Store

    constructor(props: Props) {
        super(props)
        const preLoadedState = {iceId: props.iceId, currentPage: "netwalk"}

        const isDevelopmentServer: boolean = process.env.NODE_ENV === "development"

        this.store = configureStore({
            reducer: netwalkRootReducer as Reducer<NetwalkRootState>,
            preloadedState: preLoadedState,
            middleware: (getDefaultMiddleware) => [...getDefaultMiddleware()],
            devTools: isDevelopmentServer
        })

        webSocketConnection.create(CONNECTION_TYPE_ICE, this.store, () => {
            webSocketConnection.subscribe(`/topic/ice/${props.iceId}`)
            webSocketConnection.sendObject("/av/ice/netwalk/enter", {iceId: props.iceId})
        });

        netwalkManager.init(this.store);
        terminalManager.init(this.store)
        initGenericServerActions()
        initNetwalkServerActions(this.store)
    }

    render() {
        return (
            <Provider store={this.store}>
                <NetwalkContainer/>
            </Provider>
        )
    }
}


