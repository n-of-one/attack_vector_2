import React from 'react';
import {useSelector} from "react-redux";
import {TextInput} from "../../common/component/TextInput";
import {SilentLink} from "../../common/component/SilentLink";
import {HackerState} from "../HackerRootReducer";
import {SiteInfo} from "./ScansReducer";
import {webSocketConnection} from "../../common/server/WebSocketConnection";
import {developmentServer} from "../../common/util/DevEnvironment";

/* eslint jsx-a11y/accessible-emoji: 0 */
/* eslint jsx-a11y/anchor-is-valid: 0*/

export const HackerHome = () => {

    const sites: SiteInfo[] = useSelector((state: HackerState) => state.home.scans)

    const startRunByName = (siteName: string) => {
        if (siteName) {
            webSocketConnection.send("/run/newRun", siteName)
        }
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
                           save={(siteName) => startRunByName(siteName)}
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
                                    <td className="strong">Actions</td>
                                </tr>
                                </thead>
                                <tbody>
                                {
                                    sites.map((scanInfo: SiteInfo) => {
                                        return (
                                            <tr key={scanInfo.runId}>
                                                <td className="table-very-condensed">
                                                    <SilentLink title={scanInfo.runId} onClick={() => {
                                                        prepareToEnterRun(scanInfo.runId);
                                                    }}><>{scanInfo.siteName}</>
                                                    </SilentLink>
                                                </td>
                                                <td className="table-very-condensed">{scanInfo.nodes}</td>
                                                <td className="table-very-condensed">
                                                    <DeleteScanLink runId={scanInfo.runId}/>
                                                    <ResetIceLink siteId={scanInfo.siteId}/>
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
    if (!developmentServer) {
        return <></>
    }

    const resetIce = () => {
        webSocketConnection.send("/scan/refreshIce", props.siteId)
    }

    return <>
        &nbsp;<SilentLink onClick={resetIce} title="Reset site">
        <span className="glyphicon glyphicon-refresh"/>
    </SilentLink>
    </>

}
