import React, {Component} from 'react';
import { Provider} from "react-redux";
import createSagaMiddleware from 'redux-saga'
import {applyMiddleware, createStore} from "redux";
import RequiresRole from "../common/RequiresRole";
import {REQUEST_SITE_FULL} from "../editor/EditorActions";
import editorReducer from "../editor/EditorReducer";
import initWebSocket from "../editor/EditorWebSocket";
import createSagas from "../editor/saga/EditorRootSaga";
import EditorHome from "../editor/component/EditorHome";

class RunRoot extends Component {

    constructor(props) {
        super(props);
        this.state = { initSuccess: null };
        this.errorMessage = null;
        this.siteIdentifier = props.match.params.runId;
    }

    init() {
        let runId = this.siteIdentifier;
        document.body.style.backgroundColor = "#f5f5f5";

        let preLoadedState = { };
        let sagaMiddleware = createSagaMiddleware();
        let store = createStore(editorReducer, preLoadedState, applyMiddleware(sagaMiddleware));

        let webSocketInitialized = (success) => {
            this.setState( { initSuccess: success });
            store.dispatch({type: REQUEST_SITE_FULL, runId: runId});
        };
        webSocketInitialized.bind(this);
        let stompClient = initWebSocket(store, runId, webSocketInitialized);
        let editorRootSaga = createSagas(stompClient, runId);
        sagaMiddleware.run(editorRootSaga);

        this.store = store;
    }
    renderIfAuthenticated() {
        if (this.state.initSuccess === null){
            this.init();
            return <h1>Loading...</h1>;
        }
        if (this.state.initSuccess === false) {
            return <h1>{ this.errorMessage }</h1>;
        }
        return (
            <Provider store={this.store}>
                <EditorHome />
            </Provider>
        );
    }

    render() {
        return(
            <RequiresRole>
                {this.renderIfAuthenticated()}
            </RequiresRole>
        )
    }
}


export default RunRoot

