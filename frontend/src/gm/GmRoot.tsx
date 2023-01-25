import React, {Component} from 'react'
import { GmHome } from "./component/GmHome"
import {Provider} from 'react-redux'
import {RequiresRole} from "../common/RequiresRole"
import {GM_SITES} from "./GmPages"
import {configureStore} from "@reduxjs/toolkit"
import {pageReducer, NAVIGATE_PAGE} from "../common/menu/pageReducer"
import {GmSite, gmSitesReducer} from "./GmSitesReducer"
import {Store} from "redux"


export interface GmState {
    currentPage: string,
    sites: GmSite[]
}


interface Props { }

export class GmRoot extends Component<Props>{
    gmStore: Store

    constructor(props: Props) {
        super(props)
        console.log("configure gm store")

        this.gmStore = configureStore({
            reducer: {
                currentPage: pageReducer,
                sites: gmSitesReducer,
            }
        })


        // set up initial state:
        this.gmStore.dispatch({type: NAVIGATE_PAGE, to: GM_SITES})

        document.body.style.backgroundColor = "#222222"
    }

    render() {
        return (
            <RequiresRole requires="ROLE_SITE_MANAGER">
                <Provider store={this.gmStore}>
                    <GmHome/>
                </Provider>
            </RequiresRole>
        )
    }
}

export default GmRoot
