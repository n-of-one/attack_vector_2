import React, {Component} from 'react'
import {Reducer, Store} from "redux";
import {configureStore} from "@reduxjs/toolkit";
import {webSocketConnection, WS_NETWORK_APP} from "../../../common/server/WebSocketConnection";
import {Provider} from "react-redux";
import {jigsawRootReducer, JigsawRootState} from "./reducer/JigsawRootReducer";
import {initGenericServerActions} from "../../../common/server/GenericServerActionProcessor";
import {terminalManager} from "../../../common/terminal/TerminalManager";
import {ice} from "../../StandaloneGlobals";
import {JigsawContainer} from "./component/JigsawContainer";
import {jigsawIceManager} from "./JigsawIceManager";
import {initJigsawServerActions} from "./JigsawServerActionProcessor";
import {Page} from "../../../common/menu/pageReducer";
import {initGenericAppActions} from "../../../common/server/GenericAppActionProcessor";


interface Props {
    iceId: string
    externalHack: boolean,
}

export class JigsawRoot extends Component<Props> {

    store: Store

    constructor(props: Props) {
        super(props)
        document.body.style.backgroundColor = "#333"
        ice.id = props.iceId
        const preLoadedState = {currentPage: Page.ICE}

        const isDevelopmentServer: boolean = process.env.NODE_ENV === "development"

        this.store = configureStore({
            reducer: jigsawRootReducer as Reducer<JigsawRootState>,
            preloadedState: preLoadedState,
            middleware: (getDefaultMiddleware) => [...getDefaultMiddleware()],
            devTools: isDevelopmentServer
        })

        webSocketConnection.create(WS_NETWORK_APP, this.store, () => {
            webSocketConnection.subscribe(`/topic/ice/${props.iceId}`)
            webSocketConnection.sendObject("/ice/jigsaw/enter", {iceId: ice.id})
        });

        jigsawIceManager.init(this.store, props.externalHack);
        terminalManager.init(this.store)
        initGenericServerActions({fontSize: 12})
        initGenericAppActions()
        initJigsawServerActions()
    }

    render() {
        return (
            <Provider store={this.store}>
                <JigsawContainer/>
            </Provider>
        )
    }
}
