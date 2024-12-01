import React from "react";
import {SilentLink} from "../../../../common/component/SilentLink";
import {useSelector} from "react-redux";
import {HackerRootState} from "../../../HackerRootReducer";
import {createNotification} from "../../../../common/util/Notification";
import {InfoBadge} from "../../../../common/component/ToolTip";
import {Script} from "../../../../common/script/ScriptModel";

export const ScriptPanel = () => {
    const [expanded, setExpanded] = React.useState(false)
    const currentUser = useSelector((state: HackerRootState) => state.currentUser)
    const scripts = currentUser.hacker?.scripts || []
    const toggle = () => {
        setExpanded(!expanded)
    }

    if (scripts.length === 0) {
        return <></>
    }

    if (expanded) {
        return <div className="scriptPanel">
            <ScriptsTable scripts={scripts} minimize={toggle}/>
        </div>
    } else {
        return <div className="scriptPanel">
            <ScriptsExpandButton toggle={toggle}/>
        </div>
    }
}

const ScriptsExpandButton = ({toggle}: { toggle: () => void }) => {
    return <div style={{height: "14px", width: "13px"}}>
        <div className="scriptPanelButtonOuter">
            <div className="btn btn-primary btn-s scriptPanelButton" onClick={toggle}>
                <span className="glyphicon glyphicon-expand scriptPanelButtonIcon"/>
            </div>
        </div>
    </div>
}


interface ScriptsTableProps {
    scripts: Script[]
    minimize: () => void
}


const ScriptsTable = ({scripts, minimize}: ScriptsTableProps) => {
    const loadedScripts

    return <>
        <div className="scriptPanelButtonOuter">
            <div className="btn btn-primary btn-s scriptPanelButton" onClick={minimize}>
                <span className="glyphicon glyphicon-collapse-up scriptPanelButtonIcon"/>
            </div>
        </div>
        <table className="table table-borderless table-sm text" style={{marginBottom: 0}}>
            <thead>
            <tr>
                <th colSpan={2} className="text_light">Scripts</th>
            </tr>
            </thead>
            <tbody>
            {
                scripts.map((script: Script) => {
                    return <ScriptLine script={script}/>
                })
            }
            </tbody>
        </table>
    </>
}


const ScriptLine = ({script}: { script: Script }) => {

    const strike
    const codeElement = script.usable ? <td>
        <SilentLink onClick={() => {
            copyScript(script.code)
        }}><>{script.code}</>
        </SilentLink></td> : <td className="scriptUnusable">{script.code}</td>

    const nameElement = script.usable ? <td className="text_light">{script.name}</td> : <td>{script.name}</td>


    return <tr>
        {codeElement}
        {nameElement}
        <td>{script.name}</td>
        <td>
            {script.effects.map((effect, index) => {
                const effectNumber = (index + 1).toString()
                return <InfoBadge infoText={effect} key={effectNumber} badgeText={effectNumber}/>
            })}
        </td>
    </tr>
}

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
