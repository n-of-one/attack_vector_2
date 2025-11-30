import {Script, ScriptState} from "./ScriptModel";
import {webSocketConnection} from "../server/WebSocketConnection";
import {SilentLink} from "../component/SilentLink";
import React from "react";
import {createNotification, notify} from "../util/Notification";
import {ScriptEffects} from "./type/ScriptEffects";
import {Ram} from "./ScriptStatusReducer";
import {serverTime} from "../server/ServerTime";
import {formatTimeInterval} from "../util/Util";

export enum ScriptLineUseCase {
    GM,
    HACKER
}

interface Props {
    script: Script,
    useCase: ScriptLineUseCase
    hrColor?: string
    minimize?: () => void
    ram: Ram | null
    showLoadButton: boolean
}

export const ScriptLine = ({script, useCase, minimize, ram, showLoadButton}: Props) => {
    if (ram == null) return <></>

    const forGm = useCase === ScriptLineUseCase.GM

    const actionGmLoad = forGm ? <ScriptActionGmLoad script={script} ram={ram}/> : <ScriptLoadAction script={script} ram={ram} showLoadButton={showLoadButton}/>

    return (<>
            <div className="row text" style={{marginBottom: "2px"}} data-row="hacker-script">
                <div className="col-lg-2 text-end"><CodeElement script={script} minimize={minimize}/></div>
                <div className="col-lg-2">{truncateScriptName(script.name)}</div>
                <div className="col-lg-1">{script.ram}</div>
                <div className="col-lg-2"><ScriptStateBadge script={script} /></div>
                <div className="col-lg-2 noSelect">
                    {actionGmLoad}
                    &nbsp;
                    <ScriptOfferAction script={script}/>&nbsp;
                    &nbsp;<ScriptActionDelete script={script}/>&nbsp;

                </div>
                <div className="col-lg-2"><ScriptEffects effects={script.effects}/></div>
            </div>
        </>
    )
}

const truncateScriptName = (name: string) => {
    if (name.length > 15) {
        return name.substring(0, 15) + "…"
    }
    return name
}

const CodeElement = ({script, minimize}: { script: Script, minimize?: () => void
}) => {
    if (script.state === ScriptState.EXPIRED || script.state === ScriptState.USED) {
        return <span className="text" style={{color: "#666"}} data-element="code">{script.code}</span>
    }

    return (
        <SilentLink onClick={() => {
            copyScript(script.code)
            if (minimize) minimize()
        }}><span data-element="code">{script.code}</span>
        </SilentLink>
    )}

const copyScript = (code: string) => {
    navigator.clipboard.writeText(code).then(
        () => {
            createNotification("", "copied to clipboard", 2000, "bottom-right", false)
        },
        (err) => {
            console.error("Failed to copy to clipboard", err)
        }
    )
}


export const ScriptStateBadge = ({script}: { script: Script }) => {
    switch (script.state) {
        case ScriptState.AVAILABLE:
            return <span className="badge bg-secondary scriptStatusBadge">Available</span>
        case ScriptState.OFFERING:
            return <span className="badge bg-success scriptStatusBadge">Offering</span>
        case ScriptState.LOADED:
            return <span className="badge bg-primary scriptStatusBadge">Loaded</span>
        case ScriptState.USED:
            return <span className="badge bg-dark scriptStatusBadge">Used</span>
        case ScriptState.EXPIRED:
            return <span className="badge bg-dark scriptStatusBadge">Expired</span>
        default:
            return <span>Unknown</span>
    }
}

const ScriptLoadAction = ({script, ram, showLoadButton}: { script: Script, ram: Ram, showLoadButton: boolean }) => {
    if (script.state === ScriptState.LOADED) {
        return <ScriptActionUnload script={script} ram={ram} gmOverride={false}/>
    }
    if (script.state === ScriptState.AVAILABLE && ram.free >= script.ram && showLoadButton) {
        return <ScriptActionLoad script={script} lockedUntil={ram.lockedUntil}/>
    }
    return <></>
}

// const ScriptStateBadgeLoading = ({script, loading}: { script: Script, loading?: ScriptLoading }) => {
//
//     if (!loading) return <span className="badge bg-secondary">Loading</span>
//
//     const loadPercentage = Math.round(100 * (loading.totalLoadTime - loading.secondsLeft) / loading.totalLoadTime)
//
//     return (
//         <div className="progress scriptStatusBadge" style={{
//             height: 15.28,
//             cursor: "pointer",
//             position: "relative",
//             top: "-15",
//             backgroundColor: "",
//         }}>
//
//             <div className={`progress-bar bg-primary progress-bar-striped progress-bar-animated noTransition`}
//                  role="progressbar"
//                  aria-valuenow={75} aria-valuemin={0}
//                  aria-valuemax={100} style={{width: `${loadPercentage}%`}}>
//             </div>
//
//             <div className="text" style={{
//                 position: "absolute", top: 0, paddingLeft: 16, zIndex: 100,
//                 fontSize: 10, color: "black", cursor: "pointer", userSelect: "none", pointerEvents: "none",
//             }}><strong>{loadPercentage}%</strong>
//             </div>
//
//
//         </div>)
//     // </span>
// }



const ScriptActionGmLoad = ({script, ram}: { script: Script, ram: Ram }) => {
    const action = () => {
        webSocketConnection.send("/gm/script/load", script.id)
    }
    if (script.state === ScriptState.LOADED) {
        return <ScriptActionUnload script={script} ram={ram} gmOverride={true}/>
    }
    return <SilentLink onClick={action} title="Instant load in memory"><span className="glyphicon glyphicon-save"/></SilentLink>
}

const ScriptActionUnload = ({script, ram, gmOverride}: { script: Script, ram: Ram, gmOverride: boolean }) => {
    const action = () => {
        const path = gmOverride ? "/gm/script/unload" : "/script/unload"
        webSocketConnection.send(path, script.id)
    }
    const glyphicon =  (ram.lockedUntil != null) ? "glyphicon-export" : "glyphicon-open"
    return <SilentLink onClick={action} title="Unload from memory"><span className={`glyphicon ${glyphicon}`} /></SilentLink>
}

const ScriptActionDelete = ({script}: { script: Script }) => {
    const action = () => {
        webSocketConnection.send("/script/delete", script.id)
    }
    return <SilentLink onClick={action} title="Delete"><span className="glyphicon glyphicon-trash"/></SilentLink>
}

const ScriptActionLoad = ({script, lockedUntil}: { script: Script, lockedUntil: string | null }) => {
    if (lockedUntil) {
        const action = () => {
            const lockedSecondsLeft = serverTime.secondsLeft(lockedUntil)
            const duration = formatTimeInterval(lockedSecondsLeft)
            notify({type: "neutral", message: `Cannot load, memory locked (too recently hacked, try again in: ${duration}).`})
        }
        return <SilentLink onClick={action} title="Cannot load, too recently hacked."><span className="glyphicon glyphicon-import" style={{cursor: "not-allowed"}}/></SilentLink>
    }

    const action = () => {
        webSocketConnection.send("/script/load", script.id)
    }
    return <SilentLink onClick={action} title="Start loading in memory"><span className="glyphicon glyphicon-import"/></SilentLink>
}

const ScriptOfferAction = ({script}: { script: Script }) => {
    const action = (offer: boolean) => {
        webSocketConnection.send("/script/offer", {scriptId: script.id, offer: offer})
    }
    if (script.state === ScriptState.AVAILABLE) {
        return <SilentLink onClick={() => action(true)} title="Offer script for download by another hacker"><span className="glyphicon glyphicon-share-alt"/></SilentLink>
    }
    if (script.state === ScriptState.OFFERING) {
        return <SilentLink onClick={() => action(false)} title="Stop offering script for download"><span className="glyphicon glyphicon-remove-circle"/></SilentLink>
    }
    return <></>
}
