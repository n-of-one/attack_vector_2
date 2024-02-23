import React from 'react';
import {useSelector} from "react-redux";
import {TextInput} from "../../common/component/TextInput";
import {SilentLink} from "../../common/component/SilentLink";
import {HackerState} from "../HackerRootReducer";
import {RunInfo} from "./HackerRunsReducer";
import {webSocketConnection} from "../../common/server/WebSocketConnection";
import {larp} from "../../common/Larp";

/* eslint jsx-a11y/accessible-emoji: 0 */
/* eslint jsx-a11y/anchor-is-valid: 0*/

export const HackerHome = () => {

    const runs: RunInfo[] = useSelector((state: HackerState) => state.runs)

    const startRunByName = (siteName: string) => {
        if (siteName) {
            webSocketConnection.send("/run/newRun", siteName)
        }
    }

    return (
        <div className="row">
            <div className="col-lg-2">
            </div>
            <div className="col-lg-4">
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
                           save={(siteName) => startRunByName(siteName)}
                           clearAfterSubmit={true}
                           autofocus={true}
                />
            </div>
            <div className="col-lg-6">
                <div className="row">
                    <div className="col-lg-12">
                        <div className="siteMap">
                        <table className="table table-borderless table-sm text-muted text" id="sitesTable">
                                <thead>
                                <tr>
                                    <td className="strong">Site name</td>
                                    <td className="strong">Nodes</td>
                                    <td className="strong">Actions</td>
                                </tr>
                                </thead>
                                <tbody>
                                {
                                    runs.map((runInfo: RunInfo) => {
                                        return (
                                            <tr key={runInfo.runId}>
                                                <td className="table-very-condensed">
                                                    <SilentLink title={runInfo.runId} onClick={() => {
                                                        prepareToEnterRun(runInfo.runId);
                                                    }}><>{runInfo.siteName}</>
                                                    </SilentLink>
                                                </td>
                                                <td className="table-very-condensed">{runInfo.nodes}</td>
                                                <td className="table-very-condensed">
                                                    {larp.hackersDeleteRunLinks ? <DeleteScanLink runId={runInfo.runId}/> : <></>}
                                                    {larp.hackersResetSite ? <ResetIceLink siteId={runInfo.siteId}/> : <></>}
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

export const prepareToEnterRun = (runId: string) => {
    webSocketConnection.send("/run/prepareToEnterRun", runId)
}

const DeleteScanLink = (props: { runId: string }) => {
    const deleteScan = () => {
        webSocketConnection.send("/scan/deleteScan", props.runId)
    }

    return <SilentLink onClick={deleteScan} title="Remove run">
        <span className="glyphicon glyphicon-remove-circle"/>
    </SilentLink>
}

const ResetIceLink = (props: { siteId: string }) => {
    const resetIce = () => {
        if (window.confirm(`Confirm that you want to reset this site?. This will refresh ICE, reset all timers, ...)`)) {
            webSocketConnection.send("/site/resetSite", props.siteId)
        }
    }

    return <>
        &nbsp;<SilentLink onClick={resetIce} title="Reset site">
        <span className="glyphicon glyphicon-refresh"/>
    </SilentLink>
    </>

}
