import React from 'react'
import {useDispatch, useSelector} from "react-redux"
import Terminal from "../../../common/terminal/Terminal"
import ScanCanvasPanel from "./RunCanvasPanel"
import NodeScanInfo from "./scaninfo/NodeScanInfo"
import {SUBMIT_TERMINAL_COMMAND} from "../model/RunActions"
import {HackerState} from "../../HackerRootReducer"
import {TerminalState} from "../../../common/terminal/TerminalReducer"
import {Dispatch} from "redux"


const terminalAndScanResultPanel = (infoNodeId: string | null, terminal: TerminalState, dispatch: Dispatch, submit: (command: string) => void) => {
    if (infoNodeId) {
        return (<NodeScanInfo/>)
    }
    return (<div className="row">
        <Terminal terminal={terminal} dispatch={dispatch} submit={submit} height="780px"/>
    </div>)
}


export const RunHome = () => {

    const dispatch: Dispatch = useDispatch()
    const submit = (command: string) => dispatch({type: SUBMIT_TERMINAL_COMMAND, command: command})

    const siteName = useSelector((state: HackerState) => {
        return (state.run.site.siteData) ? state.run.site.siteData.name : ""
    })
    const terminal = useSelector((state: HackerState) => state.terminal)
    const infoNodeId = useSelector((state: HackerState) => state.run.infoNodeId)

    return (
        <div className="row">
            <div className="col-lg-6">
                {terminalAndScanResultPanel(infoNodeId, terminal, dispatch, submit)}
            </div>
            <div className="col-lg-6 rightPane">

                <div className="row">
                    <div className="col-lg-12">
                        <span className="text">Site: {siteName}</span>
                    </div>
                </div>
                <div className="row">
                    <div className="col-lg-12">
                        <ScanCanvasPanel/>
                    </div>
                </div>
            </div>
        </div>
    )
}
