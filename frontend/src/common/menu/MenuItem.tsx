import React from 'react';
import {useDispatch, useSelector} from "react-redux";
import Cookies from "js-cookie";
import {NAVIGATE_PAGE, RUN} from "./pageReducer";
import {HackerState} from "../../hacker/HackerRootReducer";
import {webSocketConnection} from "../server/WebSocketConnection";
import {Dispatch} from "redux";

/* eslint jsx-a11y/anchor-is-valid: 0*/

interface Props {
    targetPage: string,
    label: string,
    requriesRole: string,
}

export const navigateTo = (currentPage: string, targetPage: string, runId: string | null, dispatch: Dispatch) => {

    if (currentPage === RUN && targetPage !== RUN) {
        webSocketConnection.send("/av/run/leaveRun", runId);
    }
    else {
        console.log("Navigating to page: " + targetPage);
        dispatch({type: NAVIGATE_PAGE, to: targetPage, from: currentPage});
    }
}

export const MenuItem = (props: Props) => {

    const dispatch = useDispatch();
    let rolesValue = Cookies.get("roles")
    let roles = (rolesValue) ? rolesValue.split("|") : []

    const currentPage =  useSelector( (state: HackerState) => state.currentPage )
    const runId: string | null =  useSelector( (state: HackerState) => state.run?.run?.runId )

    const handleClick = (event:any) => {
        event.preventDefault();
        navigateTo(currentPage, props.targetPage, runId, dispatch)
        return false
    }


    if (roles.includes(props.requriesRole)) {
        if (currentPage === props.targetPage) {
            return <li className="nav-item active"><a className="nav-link">{props.label}</a></li>
        } else {
            return <li className="nav-item"><a className="nav-link" onClick={(e) => handleClick(e)}>{props.label}</a></li>
        }
    } else {
        return <></>
    }

}
