import React from 'react';
import {useDispatch, useSelector} from "react-redux";
import {TextInput} from "../../common/component/TextInput";
import {SilentLink} from "../../common/component/SilentLink";
import {HackerState} from "../HackerRootReducer";
import {ScanInfo} from "./ScansReducer";
import {webSocketConnection} from "../../common/WebSocketConnection";
import {runCanvas} from "../run/component/RunCanvas";
import {HIDE_NODE_INFO} from "../run/model/ScanActions";
import {TERMINAL_CLEAR} from "../../common/terminal/TerminalReducer";
import {NAVIGATE_PAGE, RUN} from "../../common/menu/pageReducerX";
import {terminalManager} from "../../common/terminal/TerminalManager";
import {SERVER_SCAN_FULL, WAITING_FOR_SCAN_IGNORE_LIST} from "../server/RunServerActionProcessor";
import {Dispatch} from "redux";

/* eslint jsx-a11y/accessible-emoji: 0 */
/* eslint jsx-a11y/anchor-is-valid: 0*/

export const HackerHome = () => {

    const dispatch = useDispatch()
    const scans: ScanInfo[] = useSelector((state: HackerState) => state.home.scans)
    const currentPage: string = useSelector((state: HackerState) => state.currentPage)

    const scanSite = (siteName: string) => {
        if (siteName) {
            webSocketConnection.send("/av/scan/scanForName", siteName)
        }
    }
    const enterScanLink = (scanInfo: ScanInfo) => {
        enterScan(scanInfo.runId, scanInfo.siteId, dispatch, currentPage)
    }

    const deleteScan = (scanInfo: ScanInfo) => {
        webSocketConnection.send("/av/scan/deleteScan", scanInfo.runId)
    }

    return (
        <div className="row">
            <div className="col-lg-6">
                <div className="row">
                    <div className="col-lg-12">
                        <span className="text"><strong>🜁 Verdant OS 🜃</strong></span>
                    </div>
                </div>
                <div className="row">
                    <div className="col-lg-12">
                        <div className="text">
                            <br/>
                            Choose site to investigate or attack<br/>
                            <br/>
                            <br/>
                        </div>
                    </div>
                </div>
                <TextInput placeholder="Site name"
                           buttonLabel="Scan"
                           buttonClass="btn-info"
                           save={(siteName) => scanSite(siteName)}
                           clearAfterSubmit={true}
                           autofocus={true}
                />
            </div>
            <div className="col-lg-6">
                <div className="row">
                    <div className="col-lg-12">
                        <span className="text">Scans</span>
                    </div>
                </div>
                <div className="row">
                    <div className="col-lg-12">
                        <div className="siteMap">
                            <table className="table table-borderless table-sm text-muted text" id="sitesTable">
                                <thead>
                                <tr>
                                    <td className="strong">Site Name</td>
                                    <td className="strong">Nodes</td>
                                    <td className="strong">Initiator</td>
                                    <td className="strong">Efficiency</td>
                                    <td className="strong">&nbsp;</td>
                                </tr>
                                </thead>
                                <tbody>
                                {
                                    scans.map((scanInfo: ScanInfo) => {
                                        return (
                                            <tr key={scanInfo.runId}>
                                                <td className="table-very-condensed">
                                                    <SilentLink title={scanInfo.runId} onClick={() => {
                                                        enterScanLink(scanInfo);
                                                    }}><>{scanInfo.siteName}</>
                                                    </SilentLink>
                                                </td>
                                                <td className="table-very-condensed">{scanInfo.nodes}</td>
                                                <td className="table-very-condensed">{scanInfo.initiatorName}</td>
                                                <td className="table-very-condensed">{scanInfo.efficiency}</td>
                                                <td className="table-very-condensed">
                                                    <SilentLink onClick={() => {
                                                        deleteScan(scanInfo);
                                                    }}>
                                                        <span className="glyphicon glyphicon-remove-circle"/>
                                                    </SilentLink>
                                                </td>
                                            </tr>)
                                    })
                                }
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    )
}

export const enterScan = (runId: string, siteId: string, dispatch: Dispatch, currentPage: string) => {
    webSocketConnection.waitFor(SERVER_SCAN_FULL, WAITING_FOR_SCAN_IGNORE_LIST)
    webSocketConnection.subscribeForRun(runId, siteId)
    runCanvas.reset()
    dispatch({type: HIDE_NODE_INFO})
    dispatch({type: TERMINAL_CLEAR, terminalId: "main"})
    dispatch({type: NAVIGATE_PAGE, to: RUN, from: currentPage})
    webSocketConnection.send("/av/scan/enterScan", runId)
    terminalManager.start()
}
