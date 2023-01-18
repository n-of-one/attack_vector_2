import React from 'react'
import GmHome from "./component/GmHome";
import {Provider, useDispatch} from 'react-redux'
import RequiresRole from "../common/RequiresRole";
import gmReducer from "./GmReducer";
import {GM_SITES} from "./GmPages";
import {configureStore} from "@reduxjs/toolkit";
import {NAVIGATE_PAGE} from "../common/enums/CommonActions";

const gmStore = configureStore({
    reducer: gmReducer
});

export type GmState = ReturnType<typeof gmStore.getState>
export type GmDispatch = typeof gmStore.dispatch

export const useGmDispatch: () => GmDispatch = useDispatch

// set up initial state:
gmStore.dispatch({type: NAVIGATE_PAGE, to: GM_SITES})

document.body.style.backgroundColor = "#222222";

const GmRoot = () => {
    return(
        <RequiresRole requires="ROLE_SITE_MANAGER">
            <Provider store={gmStore}>
                <GmHome />
            </Provider>
        </RequiresRole>
    )
}

export default GmRoot
