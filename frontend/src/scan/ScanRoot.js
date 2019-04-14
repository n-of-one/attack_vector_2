import React, { Component } from 'react'
import { Provider } from 'react-redux'
import initWebSocket from "./ScanWebSocket"
import createScanSagas from "./saga/ScanRootSaga";
import createSagaMiddleware from 'redux-saga'
import {applyMiddleware, createStore} from "redux";
import RequiresRole from "../common/RequiresRole";
import ScanHome from "./component/ScanHome";
import scanReducer from "./ScanRootReducer";
import {SCAN} from "./ScanPages";
import {post} from "../common/RestClient";
import {notify, notify_fatal} from "../common/Notification";
import {REQUEST_SCAN_FULL} from "./ScanActions";

class ScanRoot extends Component {

    constructor(props) {
        super(props);
        this.state = {initSuccess: null, loginToken: null};
        this.errorMessage = null;
        let scanId = props.match.params.scanId;

        if (!scanId || !scanId.startsWith("scan-")) {
            this.state.initSuccess = false;
            this.errorMessage = "No scan ID present in URL. Found: '" + scanId + "'.";
            return;
        }

        post({
            url: "/api/scan/",
            body: {id: scanId},
            ok: ({siteId}) => { this.init(scanId, siteId) },
            notok: () => { notify_fatal("Connection to server failed, unable to continue."); }
        });
    }

    init(scanId, siteId) {
        let preLoadedState = {currentPage: SCAN};
        let sagaMiddleware = createSagaMiddleware();
        let store = createStore(scanReducer, preLoadedState, applyMiddleware(sagaMiddleware));

        let webSocketInitialized = (success) => {
            this.setState( { initSuccess: success });
            if (success) {
                store.dispatch({type: REQUEST_SCAN_FULL, scanId: scanId});
            }
        };
        webSocketInitialized.bind(this);
        let stompClient = initWebSocket(store, scanId, siteId, webSocketInitialized, store.dispatch);
        let scanRootSaga = createScanSagas(stompClient, scanId, siteId);
        sagaMiddleware.run(scanRootSaga);

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
