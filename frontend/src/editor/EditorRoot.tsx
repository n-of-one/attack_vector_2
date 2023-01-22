import React, {Component} from 'react';
import {Provider} from "react-redux";
import {EditorHome} from "./component/EditorHome";
import createSagas from "./EditorRootSaga";
import createSagaMiddleware, {SagaMiddleware} from 'redux-saga'
import editorRootReducer, {editorRootDefaultState, EditorState} from "./EditorRootReducer";
import {Reducer, Store} from "redux";
import {SERVER_SITE_FULL} from "./EditorActions";
import RequiresRole from "../common/RequiresRole";
import webSocketConnection from "../common/WebSocketConnection";
import {configureStore} from "@reduxjs/toolkit";

interface Props {
    siteId: string,
}

export let editorSiteId: string = null as unknown as string;

export class EditorRoot extends Component<Props> {

    errorMessage: string | null
    store: Store;

    constructor(props: Props) {
        super(props);

        console.log("constructing editorRoot")
        this.errorMessage = null;
        editorSiteId = props.siteId;

        if (!props.siteId.startsWith("site-")) {
            this.errorMessage = "Cannot enter site name directly in URL";
            this.state = { initSuccess: false };
            this.store = null as unknown as Store;
            return;
        }
        document.body.style.backgroundColor = "#f5f5f5";
        document.body.style.fontSize = "14px";

        const initState = editorRootDefaultState
        initState.siteData.siteId = props.siteId

        const sagaMiddleware = createSagaMiddleware() as SagaMiddleware<EditorState>;
        const developmentServer: boolean = process.env.NODE_ENV === "development";

        this.store = configureStore({
            reducer: editorRootReducer as Reducer<EditorState>,
            preloadedState: initState,
            middleware: (getDefaultMiddleware) =>  [sagaMiddleware, ...getDefaultMiddleware()],
            devTools: developmentServer
        });

        webSocketConnection.create(this.store, () => {
            webSocketConnection.subscribe('/topic/site/' + props.siteId);
            webSocketConnection.send("/av/editor/siteFull", props.siteId);
        }, SERVER_SITE_FULL);

        const editorRootSaga = createSagas();
        sagaMiddleware.run(editorRootSaga);
    }

    render() {
        if (this.errorMessage) {
            return <h1 className="text">{ this.errorMessage } <a href="/">Continue.</a></h1>;
        }
        return(
            <RequiresRole requires="ROLE_SITE_MANAGER">
                <Provider store={this.store}>
                    <EditorHome />
                </Provider>
            </RequiresRole>
        )
    }
}

