import React, {Component} from 'react';
import { Provider} from "react-redux";
import EditorHome from "./component/EditorHome";
import createSagas from "./EditorRootSaga";
import createSagaMiddleware from 'redux-saga'
import editorRootReducer from "./EditorRootReducer";
import {applyMiddleware, compose, createStore} from "redux";
import {REQUEST_SITE_FULL, SERVER_SITE_FULL} from "./EditorActions";
import RequiresRole from "../common/RequiresRole";
import webSocketConnection from "../common/WebSocketConnection";
import {siteDataDefaultState} from "./reducer/SiteDataReducer";

class EditorRoot extends Component {

    constructor(props) {
        super(props);
        this.state = { initSuccess: null };
        this.errorMessage = null;
        this.siteId = props.match.params.siteId;

        if (!this.siteId.startsWith("site-")) {
            this.errorMessage = "Cannot enter site name directly in URL";
            this.state = { initSuccess: false };
            return;
        }
        document.body.style.backgroundColor = "#f5f5f5";

        const preLoadedState = { siteData: { ...siteDataDefaultState, id: this.siteId} };
        const sagaMiddleware = createSagaMiddleware();

        const composeEnhancers = window.__REDUX_DEVTOOLS_EXTENSION_COMPOSE__ || compose;
        this.store = createStore(editorRootReducer, preLoadedState, composeEnhancers(applyMiddleware(sagaMiddleware)));

        // this.store = createStore(editorRootReducer, preLoadedState, applyMiddleware(sagaMiddleware));

        webSocketConnection.create(this.store, () => {
            webSocketConnection.subscribe('/topic/site/' + this.siteId);
            this.state = { initSuccess: true };
            this.store.dispatch({type: REQUEST_SITE_FULL, siteId: this.siteId});
        }, SERVER_SITE_FULL);

        const editorRootSaga = createSagas();
        sagaMiddleware.run(editorRootSaga);
    }

    renderIfAuthenticated() {
        if (this.state.initSuccess === false) {
            return <h1 className="text">{ this.errorMessage } <a href="/">Continue.</a></h1>;
        }
        return (
            <Provider store={this.store}>
                <EditorHome />
            </Provider>
        );
    }

    render() {
        return(
            <RequiresRole requires="ROLE_SITE_MANAGER">
                {this.renderIfAuthenticated()}
            </RequiresRole>
        )
    }
}

export default EditorRoot