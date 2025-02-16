import {Script, ScriptState} from "./ScriptModel";
import {ScriptLoading} from "./ScriptLoadingReducer";
import {webSocketConnection} from "../server/WebSocketConnection";
import {SilentLink} from "../component/SilentLink";
import React from "react";
import {createNotification} from "../util/Notification";
import {ScriptEffects} from "./ScriptEffects";

export enum ScriptLineUseCase {
    GM,
    HACKER
}

interface Props {
    script: Script,
    loading?: ScriptLoading,
    useCase: ScriptLineUseCase
    hrColor?: string
    minimize?: () => void
}

export const ScriptLine = ({script, loading, useCase, minimize}: Props) => {
    const forGm = useCase === ScriptLineUseCase.GM

    const actionInstantLoad = forGm ? <><ScriptActionInstantLoad script={script}/>&nbsp;</> : <></>
    const actionSend = forGm ? <></> : <><ScriptActionSend script={script}/>&nbsp;</>

    return (<>
            <div className="row text" style={{marginBottom: "2px"}}>
                <div className="col-lg-2 text-end"><CodeElement script={script} minimize={minimize}/></div>
                <div className="col-lg-2">{script.name}</div>
                <div className="col-lg-1">{script.ram}</div>
                <div className="col-lg-2"><ScriptStateBadge script={script} loading={loading}/></div>
                <div className="col-lg-2 noSelect">
                    {actionInstantLoad}
                    <ScriptLoadAction script={script}/>&nbsp;
                    {actionSend}
                    &nbsp;<ScriptActionDelete script={script}/>&nbsp;

                </div>
                <div className="col-lg-2"><ScriptEffects script={script}/></div>
            </div>
        </>
    )
}

const CodeElement = ({script, minimize}: { script: Script, minimize?: () => void
}) => {
    if (script.state === ScriptState.EXPIRED || script.state === ScriptState.USED) {
        return <span className="text" style={{color: "#666"}}>{script.code}</span>
    }

    return (
        <SilentLink onClick={() => {
            copyScript(script.code)
            if (minimize) minimize()
        }}><>{script.code}</>
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


export const ScriptStateBadge = ({script, loading}: { script: Script, loading?: ScriptLoading }) => {
    switch (script.state) {
        case ScriptState.AVAILABLE:
            return <span className="badge bg-secondary scriptStatusBadge">Available</span>
        case ScriptState.LOADED:
            return <span className="badge bg-primary scriptStatusBadge">Loaded</span>
        case ScriptState.USED:
            return <span className="badge bg-dark scriptStatusBadge">Used</span>
        case ScriptState.EXPIRED:
            return <span className="badge bg-dark scriptStatusBadge">Expired</span>
        case ScriptState.LOADING:
            return <ScriptStateBadgeLoading script={script} loading={loading}/>
        default:
            return <span>Unknown</span>
    }
}

const ScriptLoadAction = ({script}: { script: Script }) => {
    if (script.state === ScriptState.LOADING || script.state === ScriptState.LOADED) {
        return <ScriptActionUnload script={script}/>
    }
    if (script.state === ScriptState.AVAILABLE) {
        return <ScriptActionLoad script={script}/>
    }
    return <></>
}

const ScriptStateBadgeLoading = ({script, loading}: { script: Script, loading?: ScriptLoading }) => {

    if (!loading) return <span className="badge bg-secondary">Loading</span>

    const loadPercentage = Math.round(100 * (loading.totalLoadTime - loading.secondsLeft) / loading.totalLoadTime)

    return (
        <div className="progress scriptStatusBadge" style={{
            height: 15.28,
            cursor: "pointer",
            position: "relative",
            top: "-15",
            backgroundColor: "",
        }}>

            <div className={`progress-bar bg-primary progress-bar-striped progress-bar-animated noTransition`}
                 role="progressbar"
                 aria-valuenow={75} aria-valuemin={0}
                 aria-valuemax={100} style={{width: `${loadPercentage}%`}}>
            </div>

            <div className="text" style={{
                position: "absolute", top: 0, paddingLeft: 16, zIndex: 100,
                fontSize: 10, color: "black", cursor: "pointer", userSelect: "none", pointerEvents: "none",
            }}><strong>{loadPercentage}%</strong>
            </div>


        </div>)
    // </span>
}



const ScriptActionInstantLoad = ({script}: { script: Script }) => {
    const action = () => {
        webSocketConnection.send("/gm/script/instantLoad", script.id)
    }
    return <SilentLink onClick={action} title="Instant load in memory"><span className="glyphicon glyphicon-save"/></SilentLink>
}

const ScriptActionUnload = ({script}: { script: Script }) => {
    const action = () => {
        webSocketConnection.send("/script/unload", script.id)
    }
    return <SilentLink onClick={action} title="Unload from memory"><span className="glyphicon glyphicon-open"/></SilentLink>
}

const ScriptActionDelete = ({script}: { script: Script }) => {
    const action = () => {
        webSocketConnection.send("/script/delete", script.id)
    }
    return <SilentLink onClick={action} title="Delete"><span className="glyphicon glyphicon-trash"/></SilentLink>
}

const ScriptActionLoad = ({script}: { script: Script }) => {
    const action = () => {
        webSocketConnection.send("/script/load", script.id)
    }
    return <SilentLink onClick={action} title="Start loading in memory"><span className="glyphicon glyphicon-import"/></SilentLink>
}

const ScriptActionSend = ({script}: { script: Script }) => {
    if (script.state === ScriptState.EXPIRED || script.state === ScriptState.USED) {
        return <></>
    }
    const action = () => {
        webSocketConnection.send("/script/send", script.id)
    }
    return <SilentLink onClick={action} title="Send to other hacker"><span className="glyphicon glyphicon-share-alt"/></SilentLink>
}
