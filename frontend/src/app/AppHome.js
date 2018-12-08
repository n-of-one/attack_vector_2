import React, { Component } from 'react'
import initStore from "./store";
import GmHome from "./gm/component/GmHome";
import { Provider } from 'react-redux'

class AppRoot extends Component {

    constructor(props) {
        super(props);
        this.store = initStore();
        console.log("AppRoot constructor");
    }

    render() {
       return (
           <Provider store={this.store}>
               <GmHome />
           </Provider>
       );
    }
}

export default AppRoot

