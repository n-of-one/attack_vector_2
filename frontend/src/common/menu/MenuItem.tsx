import React from 'react';
import {useDispatch, useSelector} from "react-redux";
import Cookies from "js-cookie";
import {NAVIGATE_PAGE, RUN} from "./pageReducer";
import {webSocketConnection} from "../server/WebSocketConnection";
import {Dispatch} from "redux";
import {terminalManager} from "../terminal/TerminalManager";
import {HackerRootState} from "../../hacker/HackerRootReducer";

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

    const currentPage = useSelector((state: HackerRootState) => state.currentPage)
    const runId: string | null = useSelector((state: HackerRootState) => state.run?.run?.runId)

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

const navigateTo = (currentPage: string, targetPage: string, runId: string | null, dispatch: Dispatch) => {

    updateTerminalState(targetPage)

    if (currentPage === RUN && targetPage !== RUN) {
        webSocketConnection.send("/run/leaveSite", runId);
    }

    dispatch({type: NAVIGATE_PAGE, to: targetPage, from: currentPage});
}

export const updateTerminalState = (targetPage: string) => {
    if (targetPage === RUN) {
        terminalManager.start()
    } else {
        terminalManager.stop()
    }
}
