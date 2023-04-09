import React, {Component} from 'react'
import {Reducer, Store} from "redux";
import {configureStore} from "@reduxjs/toolkit";
import {WEBSOCKET_ICE, webSocketConnection} from "../../common/WebSocketConnection";
import {tangleIceManager} from "./component/WordSearchManager";
import {RequiresRole} from "../../common/RequiresRole";
import {Provider} from "react-redux";
import {WordSearchContainer} from "./component/WordSearchContainer";
import {wordSearchRootReducer, WordSearchRootState} from "./reducer/WordSearchRootReducer";
import {initGenericServerActions} from "../../hacker/server/GenericServerActionProcessor";
import {terminalManager} from "../../common/terminal/TerminalManager";

interface Props {
    iceId: string
}
export class WordSearchRoot extends Component<Props> {

    store: Store

    constructor(props: Props) {
        super(props)
        const preLoadedState = { iceId: props.iceId}

        const isDevelopmentServer: boolean = process.env.NODE_ENV === "development"

        this.store = configureStore({
            reducer: wordSearchRootReducer as Reducer<WordSearchRootState>,
            preloadedState: preLoadedState,
            middleware: (getDefaultMiddleware) =>  [...getDefaultMiddleware()],
            devTools: isDevelopmentServer
        })

        webSocketConnection.create(WEBSOCKET_ICE, this.store, () => {
            webSocketConnection.subscribe(`/topic/ice/${props.iceId}`)
            webSocketConnection.sendObject("/av/ice/wordsearch/enter", {iceId: props.iceId})
        });

        tangleIceManager.init(this.store);
        terminalManager.init(this.store)
        initGenericServerActions()
        // initTangleIceServerActions(this.store)
    }

    render() {
        return(
            <RequiresRole requires="ROLE_HACKER">
                <Provider store={this.store}>
                    <WordSearchContainer />
                </Provider>
            </RequiresRole>
        )
    }
}


