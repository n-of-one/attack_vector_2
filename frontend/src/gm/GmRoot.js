import React, { Component } from 'react'
import GmHome from "./component/GmHome";
import { Provider } from 'react-redux'
import RequiresRole from "../common/RequiresRole";
import {createStore} from "redux";
import gmReducer from "./GmReducer";
import {GM_SITES} from "./GmPages";

class GmRoot extends Component {

    constructor(props) {
        super(props);
        this.store = createStore(gmReducer, {currentPage: GM_SITES});
    }

    renderIfAuthenticated() {
        return (
            <Provider store={this.store}>
                <GmHome />
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

export default GmRoot

