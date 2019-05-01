import React, { Component } from 'react'
import { Provider } from 'react-redux'
import RequiresRole from "../common/RequiresRole";
import HackerHome from "./component/HackerHome";
import hackerReducer from "./HackerReducer";
import {createStore} from "redux";
import {HACKER_HOME} from "./HackerPages";
import {RECEIVE_SCANS} from "./HackerActions";
import {post} from "../common/RestClient";
import {notify_fatal} from "../common/Notification";

class HackerRoot extends Component {

    constructor(props) {
        super(props);
        this.store = createStore(hackerReducer, {currentPage: HACKER_HOME});
        post({
            url: "/api/scan/scansOfPlayer",
            body: {},
            ok: (scans) => {
                this.store.dispatch({type: RECEIVE_SCANS, data: scans});
            },
            notok: () => {
                notify_fatal("Failed to retreive scans.");
            }
        });

    }

    renderIfAuthenticated() {
        return (
            <Provider store={this.store}>
                <HackerHome />
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

export default HackerRoot

