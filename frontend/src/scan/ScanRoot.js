import React, {Component} from 'react'
import {Provider} from 'react-redux'
import initWebSocket from "../hacker/WebSocketConnection"
import createScanSagas from "../hacker/CreateHackerRootSaga";
import createSagaMiddleware from 'redux-saga'
import {applyMiddleware, createStore} from "redux";
import RequiresRole from "../common/RequiresRole";
import ScanHome from "../hacker/scan/component/ScanHome";
import scanReducer from "../hacker/scan/ScanRootReducer";
import {post} from "../common/RestClient";
import {notify_fatal} from "../common/Notification";
import {ENTER_SCAN} from "../hacker/scan/model/ScanActions";
import {SCAN} from "../hacker/HackerPages";

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
            ok: ({siteId}) => {
                this.init(scanId, siteId)
            },
            notok: () => {
                notify_fatal("There was a server problem, please try again.");
            },
            error: () => {
                notify_fatal("Connection to server failed, unable to continue.");
            }
        });
    }


    init(scanId, siteId) {
        let preLoadedState = {currentPage: SCAN};
        let sagaMiddleware = createSagaMiddleware();
        let store = createStore(scanReducer, preLoadedState, applyMiddleware(sagaMiddleware));

        let webSocketInitialized = (success) => {
            this.setState({initSuccess: success});
            if (success) {
                store.dispatch({type: ENTER_SCAN, scanId: scanId});
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

        if (this.state.initSuccess === null) {
            // this.init();
            return this.renderFake();
        }
        if (this.state.initSuccess === false) {
            return <h1 className="text">{this.errorMessage}</h1>;
        }
        return (
            <Provider store={this.store}>
                <ScanHome/>
            </Provider>
        );
    }

    render() {
        return (
            <RequiresRole requires="ROLE_HACKER">
                {this.renderIfAuthenticated()}
            </RequiresRole>
        )
    }

    renderFake() {
        return (
            <span>

            <div className="container">
                <div className="row">
                    <div className="col-lg-2">
                        <span className="text">&nbsp;</span>
                    </div>
                    <div className="col-lg-5 backgroundLight">
                        <span className="text">&nbsp;</span>
                    </div>
                    <div className="col-lg-5 rightPane">
                        <span className="text">Site: </span>
                    </div>
                </div>

                <div className="row">
                    <div className="col-lg-2">&nbsp;
                    </div>
                    <div className="col-lg-5">&nbsp;
                    </div>
                    <div className="col-lg-5 rightPane">
                        <div id="canvas-container">
                            <div id="scanCanvas" className="siteMap"/>
                        </div>
                    </div>
                </div>

            </div>
                {/*<MenuBar/>*/}
        </span>
        )
    }
}

export default ScanRoot
