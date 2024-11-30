import React from "react";
import {useSelector} from "react-redux";
import {HackerRootState} from "../HackerRootReducer";
import {Script} from "../../common/script/ScriptModel";
import {SilentLink} from "../../common/component/SilentLink";
import {webSocketConnection} from "../../common/server/WebSocketConnection";
import {ToolTip} from "../../common/component/ToolTip";


export const HackerScriptsHome = () => {

    const currentUser = useSelector((state: HackerRootState) => state.currentUser)
    const scripts = currentUser.hacker?.scripts || []

    const retrieve = () => {
        webSocketConnection.send("/hacker/script/refresh", null)
    }

    return (
        <div className="row">
            <div className="col-lg-2">
            </div>
            <div className="col-lg-4">
                <div className="row">
                    <div className="col-lg-12">
                        <span className="text">
                            <strong>🜁 Verdant OS 🜃</strong><br/>
                            <br/>
                            Scripts exploit security flaws, but those flaws will be patched automatically by security systems.
                            Sometimes immediately, sometimes after a few uses.<br/>
                            <br/>
                            Scripts can only be used on the day they are created. The daily patching cycle runs at 06:00.<br/>
                            <br/>


                            <SilentLink onClick={retrieve}>
                                <div className="btn btn-info" style={{fontSize: "12px"}}>Retrieve scripts and delete obsolete</div>
                                </SilentLink>
                            <br/>
                            <br/>
                            You have access to the following scripts each day:
                        </span>
                    </div>
                </div>
            </div>
            <div className="col-lg-6">
                <div className="row">
                    <div className="col-lg-12">
                        <div className="siteMap">
                            <table className="table table-borderless table-sm text-muted text" id="sitesTable">
                                <thead>
                                <tr>
                                    <td className="strong">Name</td>
                                    <td className="strong">Effect</td>
                                    <td className="strong">Expires in</td>
                                    <td className="strong">Code</td>
                                    <td className="strong">Action</td>
                                </tr>
                                </thead>
                                <tbody>
                                {
                                    scripts.map((script: Script) => {
                                        const usableClass = script.usable ? "" : " scriptUnusable"

                                        const deleteAction = script.usable ? <></> : <DeleteScript script={script}/>

                                        return (
                                            <tr key={script.code}>
                                                <td className={usableClass}>{script.name}</td>
                                                <td >
                                                    <ToolTip text={script.value} id={script.code}>
                                                        <span className="badge bg-secondary helpBadge">?</span>
                                                    </ToolTip>
                                                </td>
                                                <td >{script.timeLeft}</td>
                                                <td className={usableClass}><span className="scriptBadgeDark">{script.code}</span>
                                                </td>
                                                <td>{deleteAction}</td>

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

const DeleteScript = ({script}: { script: Script }) => {

    const deleteScript = () => {
        webSocketConnection.send("/hacker/script/delete", script.code)
    }

    return <SilentLink onClick={deleteScript} title="Remove script">
        <span className="glyphicon glyphicon-trash"/>
    </SilentLink>
}
