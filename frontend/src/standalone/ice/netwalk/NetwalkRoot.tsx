import React, {Component} from 'react'
import {Reducer, Store} from "redux";
import {configureStore} from "@reduxjs/toolkit";
import {webSocketConnection, WS_NETWORK_APP} from "../../../common/server/WebSocketConnection";
import {Provider} from "react-redux";
import {NetwalkContainer} from "./component/NetwalkContainer";
import {netwalkRootReducer, NetwalkRootState} from "./reducer/NetwalkRootReducer";
import {initGenericServerActions} from "../../../common/server/GenericServerActionProcessor";
import {terminalManager} from "../../../common/terminal/TerminalManager";
import {initNetwalkServerActions} from "./NetwalkServerActionProcessor";
import {netwalkManager} from "./NetwalkManager";
import {ice} from "../../StandaloneGlobals";
import {Page} from "../../../common/menu/pageReducer";
import {initGenericAppActions} from "../../../common/server/GenericAppActionProcessor";

interface Props {
    iceId: string
    externalHack: boolean,
}

export class NetwalkRoot extends Component<Props> {

    store: Store

    constructor(props: Props) {
        super(props)
        ice.id = props.iceId
        const preLoadedState = {currentPage: Page.ICE}

        const isDevelopmentServer: boolean = process.env.NODE_ENV === "development"

        this.store = configureStore({
            reducer: netwalkRootReducer as Reducer<NetwalkRootState>,
            preloadedState: preLoadedState,
            middleware: (getDefaultMiddleware) => [...getDefaultMiddleware()],
            devTools: isDevelopmentServer
        })

        webSocketConnection.create(WS_NETWORK_APP, this.store, () => {
            webSocketConnection.subscribe(`/topic/ice/${props.iceId}`)
            webSocketConnection.sendObject("/ice/netwalk/enter", {iceId: ice.id})
        });

        netwalkManager.init(this.store, props.externalHack);
        terminalManager.init(this.store)
        initGenericServerActions({fontSize: 12})
        initGenericAppActions()
        initNetwalkServerActions()
    }

    render() {
        return (
            <Provider store={this.store}>
                <NetwalkContainer/>
            </Provider>
        )
    }
}


