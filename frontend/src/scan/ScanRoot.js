import React, {Component} from 'react'
import {Provider} from 'react-redux'
import initWebSocket from "./ScanWebSocket"
import createScanSagas from "./saga/ScanRootSaga";
import createSagaMiddleware from 'redux-saga'
import {applyMiddleware, createStore} from "redux";
import RequiresRole from "../common/RequiresRole";
import ScanHome from "./component/ScanHome";
import scanReducer from "./ScanRootReducer";
import {post} from "../common/RestClient";
import {notify_fatal} from "../common/Notification";
import {ENTER_SCAN} from "./ScanActions";
import {SCAN} from "../hacker/HackerPages";
import {TERMINAL_KEY_PRESS, TERMINAL_SUBMIT, TERMINAL_TICK} from "../common/terminal/TerminalActions";
import {ENTER_KEY, F12_KEY, F2_KEY} from "../KeyCodes";

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

    handleKeyDown(event) {
        let {keyCode, key} = event;
        if (keyCode >= F2_KEY && keyCode <= F12_KEY) {
            return;
        }

        event.preventDefault();
        if (keyCode === ENTER_KEY) {
            this.store.dispatch({type: TERMINAL_SUBMIT, key: key, command: this.store.getState().terminal.input, terminalId: "main"});
        }
        else {
            this.store.dispatch({type: TERMINAL_KEY_PRESS, key: key, keyCode: keyCode, terminalId: "main"});
        }
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

            setInterval(() => {
                this.store.dispatch({type: TERMINAL_TICK});
            }, 10);

            window.onkeydown= (event) => {
                this.handleKeyDown(event);
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
