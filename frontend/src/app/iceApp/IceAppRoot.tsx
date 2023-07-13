import React, {Component} from 'react'
import {Reducer, Store} from "redux";
import {configureStore} from "@reduxjs/toolkit";
import {webSocketConnection, WS_NETWORK_APP} from "../../common/server/WebSocketConnection";
import {Provider} from "react-redux";
import {initGenericServerActions} from "../../hacker/server/GenericServerActionProcessor";
import {terminalManager} from "../../common/terminal/TerminalManager";
import {iceAppRootReducer, IceAppRootState} from "./reducer/IceAppRootReducer";
import {initIceAppServerActions} from "./IceAppServerActionProcessor";
import {IceAppContainer} from "./component/IceAppContainer";
import {ice} from "../../ice/IceModel";

interface Props {
    iceId: string
    layerId: string
}

export class IceAppRoot extends Component<Props> {

    store: Store

    constructor(props: Props) {
        super(props)
        ice.id = props.iceId
        const preLoadedState = {currentPage: "iceApp", layerId: props.layerId}

        const isDevelopmentServer: boolean = process.env.NODE_ENV === "development"

        this.store = configureStore({
            reducer: iceAppRootReducer as Reducer<IceAppRootState>,
            preloadedState: preLoadedState,
            middleware: (getDefaultMiddleware) => [...getDefaultMiddleware()],
            devTools: isDevelopmentServer
        })

        webSocketConnection.create(WS_NETWORK_APP, this.store, () => {
            webSocketConnection.subscribe(`/topic/ice/${ice.id}`)
            webSocketConnection.sendObject("/av/ice/password/enter", {iceId: ice.id, userType: "USER"})
        });

        terminalManager.init(this.store)
        initGenericServerActions()
        initIceAppServerActions(this.store)

        document.body.style.backgroundColor = "#333";
    }

    render() {
        return (
            <Provider store={this.store}>
                <IceAppContainer/>
            </Provider>
        )
    }
}
