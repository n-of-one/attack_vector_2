import React, {Component} from 'react'
import {Reducer, Store} from "redux";
import {configureStore} from "@reduxjs/toolkit";
import {webSocketConnection} from "../../common/server/WebSocketConnection";
import {Provider} from "react-redux";
import {WordSearchContainer} from "./component/WordSearchContainer";
import {wordSearchRootReducer, WordSearchRootState} from "./reducer/WordSearchRootReducer";
import {initGenericServerActions} from "../../hacker/server/GenericServerActionProcessor";
import {terminalManager} from "../../common/terminal/TerminalManager";
import {wordSearchManager} from "./component/WordSearchManager";
import {initWordSearchServerActions} from "./WordSearchServerActionProcessor";
import {NETWORKED_APP_ENDPOINT} from "../../common/server/ConnectionType";

interface Props {
    iceId: string
}
export class WordSearchRoot extends Component<Props> {

    store: Store

    constructor(props: Props) {
        super(props)
        const preLoadedState = { iceId: props.iceId, currentPage: "wordSearch"}

        const isDevelopmentServer: boolean = process.env.NODE_ENV === "development"

        this.store = configureStore({
            reducer: wordSearchRootReducer as Reducer<WordSearchRootState>,
            preloadedState: preLoadedState,
            middleware: (getDefaultMiddleware) =>  [...getDefaultMiddleware()],
            devTools: isDevelopmentServer
        })

        webSocketConnection.create(NETWORKED_APP_ENDPOINT, this.store, () => {
            webSocketConnection.subscribe(`/topic/ice/${props.iceId}`)
            webSocketConnection.sendObject("/av/ice/wordSearch/enter", {iceId: props.iceId})
        });

        wordSearchManager.init(this.store);
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


