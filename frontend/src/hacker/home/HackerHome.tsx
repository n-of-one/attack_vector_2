import React from 'react';
import {useSelector} from "react-redux";
import {TextInput} from "../../common/component/TextInput";
import {SilentLink} from "../../common/component/SilentLink";
import {HackerRootState} from "../HackerRootReducer";
import {RunInfo} from "./HackerRunsReducer";
import {webSocketConnection} from "../../common/server/WebSocketConnection";
import {ConfigItem, ConfigRootState, getConfigAsBoolean} from "../../admin/config/ConfigReducer";
import {HackerSkill} from "../../common/users/UserReducer";

/* eslint jsx-a11y/accessible-emoji: 0 */
/* eslint jsx-a11y/anchor-is-valid: 0*/

export const HackerHome = () => {

    const runs: RunInfo[] = useSelector((state: HackerRootState) => state.runs)
    const config = useSelector((rootState: ConfigRootState) => rootState.config)
    const hackersResetSite = (getConfigAsBoolean(ConfigItem.DEV_HACKER_RESET_SITE, config))
    const hackerDeleteRunLinks = (getConfigAsBoolean(ConfigItem.HACKER_DELETE_RUN_LINKS, config))




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
                <SearchSite/>
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
                                                    {hackerDeleteRunLinks ? <DeleteRunLink runId={runInfo.runId}/> : <></>}
                                                    {hackersResetSite ? <ResetIceLink siteId={runInfo.siteId} siteName={runInfo.siteName}/> : <></>}
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

const SearchSite = () => {

    const skills = useSelector((state: HackerRootState) => state.skills)
    if (skills === null) return <></> // waiting for skills to be received

    const canSearchSite = skills.includes(HackerSkill.SEARCH_SITE)

    if (!canSearchSite) {
        return (
            <div className="row">
                <div className="col-lg-12">
                    <div className="text">
                        <br/>
                        Choose a run on to join, or ask a fellow hacker to /share a run with you.<br/>
                    </div>
                </div>
            </div>
        )
    }

    const startRunByName = (siteName: string) => {
        if (siteName) {
            webSocketConnection.send("/run/newRun", siteName)
        }
    }
    return (<>
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
    </>)

}

export const prepareToEnterRun = (runId: string) => {
    webSocketConnection.send("/run/prepareToEnterRun", runId)
}

const DeleteRunLink = (props: { runId: string }) => {
    const deleteScan = () => {
        webSocketConnection.send("/run/deleteRunLink", props.runId)
    }

    return <SilentLink onClick={deleteScan} title="Remove run">
        <span className="glyphicon glyphicon-remove-circle"/>
    </SilentLink>
}

const ResetIceLink = (props: { siteId: string, siteName: string }) => {
    const resetIce = () => {
        if (window.confirm(`Confirm that you want to reset "${props.siteName}". This will refresh ICE, reset all timers, ...)`)) {
            webSocketConnection.send("/site/resetSite", props.siteId)
        }
    }

    return <>
        &nbsp;<SilentLink onClick={resetIce} title="Reset site">
        <span className="glyphicon glyphicon-refresh"/>
    </SilentLink>
    </>

}
