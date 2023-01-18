import React from 'react'
import { GmHome } from "./component/GmHome";
import {Provider, useDispatch} from 'react-redux'
import RequiresRole from "../common/RequiresRole";
import {GM_SITES} from "./GmPages";
import {configureStore} from "@reduxjs/toolkit";
import {pageReducer, NAVIGATE_PAGE} from "../common/menu/pageReducer";
import {gmSitesReducer} from "./GmSitesReducer";

const gmStore = configureStore({
    reducer: {
        currentPage: pageReducer,
        sites: gmSitesReducer,
    }
});

export type GmState = ReturnType<typeof gmStore.getState>
export type GmDispatch = typeof gmStore.dispatch

export const useGmDispatch: () => GmDispatch = useDispatch

// set up initial state:
gmStore.dispatch({type: NAVIGATE_PAGE, to: GM_SITES})

document.body.style.backgroundColor = "#222222";

const GmRoot = () => {
    return (
        <RequiresRole requires="ROLE_SITE_MANAGER">
            <Provider store={gmStore}>
                <GmHome/>
            </Provider>
        </RequiresRole>
    )
}

export default GmRoot
