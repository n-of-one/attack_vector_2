import React, {Component} from 'react'
import {Reducer, Store} from "redux";
import {configureStore} from "@reduxjs/toolkit";
import {webSocketConnection, WS_NETWORK_APP} from "../../../common/server/WebSocketConnection";
import {Provider} from "react-redux";
import {WordSearchContainer} from "./component/WordSearchContainer";
import {wordSearchRootReducer, WordSearchRootState} from "./reducer/WordSearchRootReducer";
import {initGenericServerActions} from "../../../hacker/server/GenericServerActionProcessor";
import {terminalManager} from "../../../common/terminal/TerminalManager";
import {wordSearchManager} from "./WordSearchManager";
import {initWordSearchServerActions} from "./WordSearchServerActionProcessor";
import {ice} from "../IceModel";

interface Props {
    iceId: string
    nextUrl: string | null
}
export class WordSearchRoot extends Component<Props> {

    store: Store

    constructor(props: Props) {
        super(props)
        ice.id = props.iceId
        const preLoadedState = { iceId: props.iceId, currentPage: "wordSearch"}

        const isDevelopmentServer: boolean = process.env.NODE_ENV === "development"

        this.store = configureStore({
            reducer: wordSearchRootReducer as Reducer<WordSearchRootState>,
            preloadedState: preLoadedState,
            middleware: (getDefaultMiddleware) =>  [...getDefaultMiddleware()],
            devTools: isDevelopmentServer
        })

        webSocketConnection.create(WS_NETWORK_APP, this.store, () => {
            webSocketConnection.subscribe(`/topic/ice/${ice.id}`)
            webSocketConnection.sendObject("/av/ice/wordSearch/enter", {iceId: ice.id})
        });

        wordSearchManager.init(this.store, props.nextUrl);
        terminalManager.init(this.store)
        initGenericServerActions()
        initWordSearchServerActions(this.store)
    }

    render() {
        return(
            <Provider store={this.store}>
                <WordSearchContainer />
            </Provider>
        )
    }
}


