import React, {Component} from 'react'
import {Reducer, Store} from "redux";
import {configureStore} from "@reduxjs/toolkit";
import {webSocketConnection} from "../../common/WebSocketConnection";
import {RequiresRole} from "../../common/RequiresRole";
import {Provider} from "react-redux";
import {WordSearchContainer} from "./component/WordSearchContainer";
import {wordSearchRootReducer, WordSearchRootState} from "./reducer/WordSearchRootReducer";
import {initGenericServerActions} from "../../hacker/server/GenericServerActionProcessor";
import {terminalManager} from "../../common/terminal/TerminalManager";
import {wordSearchManager} from "./component/WordSearchManager";
import {initWordSearchServerActions} from "./WordSearchServerActionProcessor";
import {CONNECTION_TYPE_ICE} from "../../common/CurrentUser";

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

        webSocketConnection.create(CONNECTION_TYPE_ICE, this.store, () => {
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
            <RequiresRole requires="ROLE_HACKER">
                <Provider store={this.store}>
                    <WordSearchContainer />
                </Provider>
            </RequiresRole>
        )
    }
}


