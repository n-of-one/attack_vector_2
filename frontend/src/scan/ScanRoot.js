import React, { Component } from 'react'
import { Provider } from 'react-redux'
import initWebSocket from "./ScanWebSocket"
import createScanSagas from "./saga/ScanRootSaga";
import createSagaMiddleware from 'redux-saga'
import {applyMiddleware, createStore} from "redux";
import RequiresRole from "../common/RequiresRole";
import ScanHome from "./component/ScanHome";
import scanReducer from "./ScanReducer";
import {SCAN} from "./ScanPages";
import {REQUEST_SITE_FULL} from "../editor/EditorActions";

class ScanRoot extends Component {

    constructor(props) {
        super(props);
        this.state = { initSuccess: null, loginToken: null };
        this.errorMessage = null;
        let siteId = props.match.params.siteId;

        if (!siteId || !siteId.startsWith("scan-")) {
            this.state.initSuccess = false;
            this.errorMessage = "No scan ID present in URL. Found: '" + siteId + "'.";
            return;
        }
        let preLoadedState = {currentPage: SCAN};
        let sagaMiddleware = createSagaMiddleware();
        let store = createStore(scanReducer, preLoadedState, applyMiddleware(sagaMiddleware));

        let webSocketInitialized = (success) => {
            this.setState( { initSuccess: success });
            // store.dispatch({type: REQUEST_SITE_FULL, siteId: siteId});
        };
        webSocketInitialized.bind(this);
        let stompClient = initWebSocket(store, siteId, webSocketInitialized, store.dispatch);
        let editorRootSaga = createScanSagas(stompClient, siteId);
        sagaMiddleware.run(editorRootSaga);

        this.store = store;
    }

    renderIfAuthenticated() {
        document.body.style.backgroundColor = "#222222";

        if (this.state.initSuccess === null){
            // this.init();
            return <h1 className="text">Loading...</h1>;
        }
        if (this.state.initSuccess === false) {
            return <h1 className="text">{ this.errorMessage }</h1>;
        }
        return (
            <Provider store={this.store}>
                <ScanHome />
            </Provider>
        );
    }

    render() {
        return(
            <RequiresRole requires="ROLE_HACKER">
                {this.renderIfAuthenticated()}
            </RequiresRole>
        )
    }
}

export default ScanRoot
