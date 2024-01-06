import React from 'react'
import {useDispatch, useSelector} from "react-redux"
import {Terminal} from "../../../common/terminal/Terminal"
import {RunCanvasPanel} from "./RunCanvasPanel"
import {NodeScanInfo} from "./scaninfo/NodeScanInfo"
import {HackerState} from "../../HackerRootReducer"
import {TERMINAL_SUBMIT, TerminalState} from "../../../common/terminal/TerminalReducer"
import {Dispatch} from "redux"
import {Timers} from "../coundown/Timers";
import {ENTER_KEY} from "../../../common/util/KeyCodes";
import {webSocketConnection} from "../../../common/server/WebSocketConnection";
import {currentUser} from "../../../common/user/CurrentUser";


const TerminalAndScanResultPanel = (infoNodeId: string | null, terminal: TerminalState, submit: () => void) => {

    const timerCount = useSelector((state: HackerState) => state.run.timers.length)
    const adjustedTimerCount = timerCount > 0 ? timerCount : 1

    if (infoNodeId) {
        return (<NodeScanInfo/>)
    }

    const terminalHeight = 847 - (adjustedTimerCount * 24) - 23

    return <>
        <Timers/>
        <div className="row">
            <Terminal terminalState={terminal} submit={submit} height={terminalHeight}/>
        </div>
    </>
}


export const RunHome = () => {

    const dispatch: Dispatch = useDispatch()

    const siteName = useSelector((state: HackerState) => {
        return (state.run.site.siteProperties) ? state.run.site.siteProperties.name : ""
    })
    const terminal = useSelector((state: HackerState) => state.terminal)
    const submit = () => {
        const payload = {command: terminal.input};
        webSocketConnection.sendObjectWithRunId("/terminal/main", payload);

        dispatch({type: TERMINAL_SUBMIT, key: ENTER_KEY, command: terminal.input, terminalId: terminal.id});
    }
    const infoNodeId = useSelector((state: HackerState) => state.run.infoNodeId)

    return (
        <div className="row">
            <div className="col-lg-6">
                {TerminalAndScanResultPanel(infoNodeId, terminal, submit)}
            </div>
            <div className="col-lg-6">
                <div className="row">
                    <div className="col-lg-12">
                        <span className="text">Site: {siteName}</span>
                    </div>
                </div>
                <div className="row">
                    <div className="col-lg-12">
                        <RunCanvasPanel dispatch={dispatch} userId={currentUser.id}/>
                    </div>
                </div>
            </div>
        </div>
    )
}
