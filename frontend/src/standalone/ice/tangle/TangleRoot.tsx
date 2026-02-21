import React, {Component} from 'react'
import {Reducer, Store} from "redux";
import {configureStore} from "@reduxjs/toolkit";
import {webSocketConnection, WS_NETWORK_APP} from "../../../common/server/WebSocketConnection";
import {tangleIceManager} from "./TangleIceManager";
import {Provider} from "react-redux";
import {TangleContainer} from "./component/TangleContainer";
import {tangleRootReducer, TangleRootState} from "./reducer/TangleRootReducer";
import {initGenericServerActions} from "../../../common/server/GenericServerActionProcessor";
import {terminalManager} from "../../../common/terminal/TerminalManager";
import {ice} from "../../StandaloneGlobals";
import {Page} from "../../../common/menu/pageReducer";
import {initTangleIceServerActions} from "./TangleServerActionProcessor";

interface Props {
    iceId: string,
    externalHack: boolean,
}
export class TangleRoot extends Component<Props> {

    store: Store

    constructor(props: Props) {
        super(props)
        ice.id = props.iceId
        const preLoadedState = {currentPage: Page.ICE}

        const isDevelopmentServer: boolean = process.env.NODE_ENV === "development"

        this.store = configureStore({
            reducer: tangleRootReducer as Reducer<TangleRootState>,
            preloadedState: preLoadedState,
            middleware: (getDefaultMiddleware) =>  [...getDefaultMiddleware()],
            devTools: isDevelopmentServer
        })

        webSocketConnection.create(WS_NETWORK_APP, this.store, () => {
            webSocketConnection.subscribe(`/topic/ice/${props.iceId}`)
            webSocketConnection.sendObject("/ice/tangle/enter", {iceId: props.iceId})
        });

        tangleIceManager.init(this.store, props.externalHack)
        terminalManager.init(this.store)
        initGenericServerActions({fontSize: 12})
        initTangleIceServerActions()
    }

    render() {
        return(
            <Provider store={this.store}>
                <TangleContainer />
            </Provider>
        )
    }


}


