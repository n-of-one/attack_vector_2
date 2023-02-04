import React from 'react'
import {useDispatch, useSelector} from "react-redux"
import {Terminal} from "../../../common/terminal/Terminal"
import {RunCanvasPanel} from "./RunCanvasPanel"
import {NodeScanInfo} from "./scaninfo/NodeScanInfo"
import {HackerState} from "../../HackerRootReducer"
import {TERMINAL_SUBMIT, TerminalState} from "../../../common/terminal/TerminalReducer"
import {Dispatch} from "redux"
import {CountdownTimer} from "../coundown/CountdownTimer";
import {ENTER_KEY} from "../../../KeyCodes";
import {webSocketConnection} from "../../../common/WebSocketConnection";


const terminalAndScanResultPanel = (infoNodeId: string | null, terminal: TerminalState, submit: () => void) => {
    if (infoNodeId) {
        return (<NodeScanInfo/>)
    }
    return (<>
            <div className="row">
                <CountdownTimer/>
            </div>
            <div className="row">
                <Terminal terminalState={terminal} submit={submit} height="780px"/>
            </div>
        </>
    )
}


export const RunHome = () => {

    const dispatch: Dispatch = useDispatch()

    const siteName = useSelector((state: HackerState) => {
        return (state.run.site.siteData) ? state.run.site.siteData.name : ""
    })
    const terminal = useSelector((state: HackerState) => state.terminal)
    const submit = () => {
        const payload = {command: terminal.input};
        webSocketConnection.sendObjectWithRunId("/av/terminal/main", payload);

        dispatch({type: TERMINAL_SUBMIT, key: ENTER_KEY, command: terminal.input, terminalId: terminal.id});
    }
    const infoNodeId = useSelector((state: HackerState) => state.run.infoNodeId)
    const userId = useSelector((state: HackerState) => state.userId)

    return (
        <div className="row">
            <div className="col-lg-6">
                {terminalAndScanResultPanel(infoNodeId, terminal, submit)}
            </div>
            <div className="col-lg-6 rightPane">

                <div className="row">
                    <div className="col-lg-12">
                        <span className="text">Site: {siteName}</span>
                    </div>
                </div>
                <div className="row">
                    <div className="col-lg-12">
                        <RunCanvasPanel dispatch={dispatch} userId={userId}/>
                    </div>
                </div>
            </div>
        </div>
    )
}
