import React from 'react';
import {useDispatch, useSelector} from "react-redux";
import Cookies from "js-cookie";
import {NAVIGATE_PAGE, SCAN} from "./pageReducer";
import {HackerState} from "../../hacker/HackerRootReducer";
import {webSocketConnection} from "../WebSocketConnection";
import {terminalManager} from "../terminal/TerminalManager";
import {runCanvas} from "../../hacker/run/component/RunCanvas";

/* eslint jsx-a11y/anchor-is-valid: 0*/

interface Props {
    targetPage: string,
    label: string,
    requriesRole: string,
}

export const MenuItem = (props: Props) => {

    const dispatch = useDispatch();
    let rolesValue = Cookies.get("roles")
    let roles = (rolesValue) ? rolesValue.split("|") : []

    const currentPage =  useSelector( (state: HackerState) => state.currentPage )
    const runId: string | null =  useSelector( (state: HackerState) => state.run?.scan?.runId )

    const navigateTo = (event: any, targetPage: string) => {
        event.preventDefault();
        console.log("Navigating to page: " + targetPage);
        dispatch({type: NAVIGATE_PAGE, to: targetPage, from: currentPage});

        if (currentPage === SCAN && targetPage !== SCAN) {
            webSocketConnection.unsubscribe();
            terminalManager.stop();
            runCanvas.stop();
            webSocketConnection.send("/av/scan/leaveScan", runId);
        }
        return false
    }



    if (roles.includes(props.requriesRole)) {
        if (currentPage === props.targetPage) {
            return <li className="nav-item active"><a className="nav-link">{props.label}</a></li>
        } else {
            return <li className="nav-item"><a className="nav-link" onClick={(e) => navigateTo(e, props.targetPage)}>{props.label}</a></li>
        }
    } else {
        return <></>
    }

}
