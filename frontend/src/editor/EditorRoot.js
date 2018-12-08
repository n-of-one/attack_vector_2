import React, {Component} from 'react';
import initWebSocket from "./EditorWebSocket"
import { Provider} from "react-redux";
import EditorHome from "./component/EditorHome";
import createSagas from "./saga/EditorRootSaga";
import createSagaMiddleware from 'redux-saga'
import editorReducer from "./MyEditorStore";
import {applyMiddleware, createStore} from "redux";
import { REQUEST_SITE_FULL} from "./EditorActions";

class EditorRoot extends Component {

    constructor(props) {
        super(props);
        this.state = { initSuccess: null };
        this.errorMessage = null;
        this.init(props, props.match.params.siteId);
    }

    init(props, siteIdentifier) {
        if (!siteIdentifier.startsWith("site-")) {
            fetch("/api/site/" + siteIdentifier)
                .then( response => {
                    if (response.ok) {
                        response.text().then(siteId => {
                            document.location.href = siteId;
                        });
                    }
                    else {
                        this.errorMessage = "Connection to server failed, unable to continue.";
                        this.setState({ initSuccess: false });
                    }
                });
            return;
        }

        let wsInit = (result) => { this.setState( { initSuccess: result }); };
        wsInit.bind(this);

        fetch("/api/health/")
            .then( response => {
                if (response.ok) {
                    this.initServerState(siteIdentifier);
                }
                else {
                    this.errorMessage = "Connection to server failed, unable to continue.";
                    wsInit(false);
                }
            }
        );
    }

    initServerState(siteId) {
        document.body.style.backgroundColor = "#f5f5f5";

        let preLoadedState = { };
        let sagaMiddleware = createSagaMiddleware();
        let store = createStore(editorReducer, preLoadedState, applyMiddleware(sagaMiddleware));

        let webSocketInitialized = (success) => {
            this.setState( { initSuccess: success });
            store.dispatch({type: REQUEST_SITE_FULL, siteId: siteId});
        };
        webSocketInitialized.bind(this);

        let stompClient = initWebSocket(store, siteId, webSocketInitialized);
        let editorRootSaga = createSagas(stompClient, siteId);
        sagaMiddleware.run(editorRootSaga);

        this.store = store;
    }

    render() {
        if (this.state.initSuccess === null){
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
}


export default EditorRoot

