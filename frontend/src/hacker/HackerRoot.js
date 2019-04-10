import React, { Component } from 'react'
import { Provider } from 'react-redux'
import RequiresRole from "../common/RequiresRole";
import HackerHome from "./component/HackerHome";
import hackerReducer from "./HackerReducer";
import {createStore} from "redux";
import {HACKER_HOME} from "./HackerPages";

class HackerRoot extends Component {

    constructor(props) {
        super(props);
        this.store = createStore(hackerReducer, {currentPage: HACKER_HOME});
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

