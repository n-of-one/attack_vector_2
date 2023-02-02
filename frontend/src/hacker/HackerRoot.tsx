import React, {Component} from 'react'
import {Provider} from 'react-redux'
import {RequiresRole} from "../common/RequiresRole";
import {Reducer, Store} from "redux";
import {HackerPageChooser} from "./HackerPageChooser";
import createSagaMiddleware, {SagaMiddleware} from 'redux-saga'
import createHackerRootSaga from "./HackerRootSaga";
import {hackerRootReducer, HackerState} from "./HackerRootReducer";
import {webSocketConnection} from "../common/WebSocketConnection";
import {terminalManager} from "../common/terminal/TerminalManager";
import {RETRIEVE_USER_SCANS} from "./home/HomeActions";
import {passwordIceManager} from "./run/ice/password/PasswordIceManager";
import {tangleIceManager} from "./run/ice/tangle/TangleIceManager";
import {configureStore} from "@reduxjs/toolkit";
import {HACKER_HOME} from "../common/menu/pageReducer";

export class HackerRoot extends Component {

    store: Store

    constructor(props: {}) {
        super(props)
        const preLoadedState = {currentPage: HACKER_HOME};
        const sagaMiddleware = createSagaMiddleware() as SagaMiddleware<HackerState>;

        const isDevelopmentServer: boolean = process.env.NODE_ENV === "development"
        // const defaultMiddlewareOptions = isDevelopmentServer ? {
        //     immutableCheck: { warnAfter: 2 },
        //     serializableCheck: { warnAfter: 2 },
        // } : {
        //     immutableCheck: false,
        //     serializableCheck: false,
        // }


        this.store = configureStore({
            reducer: hackerRootReducer as Reducer<HackerState>,
            preloadedState: preLoadedState,

            middleware: (getDefaultMiddleware) =>  [sagaMiddleware, ...getDefaultMiddleware()],
            // TODO: check if we ever got high values for the immutableCheck (> 32 ms)
            // middleware: (getDefaultMiddleware) =>  [sagaMiddleware, ...getDefaultMiddleware(defaultMiddlewareOptions)],
            devTools: isDevelopmentServer
        })

        // this.store = createStore(hackerRootReducer, preLoadedState, composeEnhancers(applyMiddleware(sagaMiddleware)));
        // this.store = createStore(hackerRootReducer, preLoadedState, applyMiddleware(sagaMiddleware));

        webSocketConnection.create(this.store, () => {
            this.store.dispatch({type: RETRIEVE_USER_SCANS});
        });

        const scanRootSaga = createHackerRootSaga();
        sagaMiddleware.run(scanRootSaga);

        terminalManager.init(this.store);
        passwordIceManager.init(this.store);
        tangleIceManager.init(this.store);
    }

    render() {
        return(
            <RequiresRole requires="ROLE_HACKER">
                <Provider store={this.store}>
                    <HackerPageChooser />
                </Provider>
            </RequiresRole>
        )
    }
}
