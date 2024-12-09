import React, {useState} from "react";
import {useSelector} from "react-redux";
import {HackerRootState} from "../../../HackerRootReducer";
import {Script, ScriptState} from "../../../../common/script/ScriptModel";
import {ScriptLine, ScriptLineUseCase} from "../../../../common/script/ScriptLine";
import {GmRootState} from "../../../../gm/GmRootReducer";
import {DataTable} from "../../../../common/component/dataTable/DataTable";
import {Hr} from "../../../../common/component/dataTable/Hr";

export const ScriptPanel = () => {
    const [expanded, setExpanded] = React.useState(false)
    const currentUser = useSelector((state: HackerRootState) => state.currentUser)
    const [onlyShowLoaded, setOnlyShowLoaded] = useState(true)

    const scripts = currentUser.hacker?.scripts || []

    const expand = () => {
        setExpanded(true)
    }
    const minimize = () => {
        setExpanded(false)
    }

    if (scripts.length === 0) {
        return <></>
    }

    const filteredScripts = onlyShowLoaded ? scripts.filter(script => script.state === ScriptState.LOADED || script.state === ScriptState.LOADING) : scripts

    const hr = <Hr color="#666"/>

    if (expanded) {
        return <div className="scriptPanel" style={{width: "780px"}}>
            <ScriptMinimizeButton minimize={minimize} onlyShowLoaded={onlyShowLoaded} setOnlyShowLoaded={setOnlyShowLoaded}/>
            <ScriptsTable scripts={filteredScripts} hr={hr}/>
        </div>
    } else {
        return <div className="scriptPanel">
            <ScriptsExpandButton expand={expand}/>
        </div>
    }
}

const ScriptsExpandButton = ({expand}: { expand: () => void }) => {
    return <div style={{height: "14px", width: "13px"}}>
        <div className="scriptPanelButtonOuter">
            <div className="btn btn-primary btn-s scriptPanelButton" onClick={expand}>
                <span className="glyphicon glyphicon-expand scriptPanelButtonIcon"/>
            </div>
        </div>
    </div>
}

interface ScriptMinimizeButtonProps {
    minimize: () => void
    onlyShowLoaded: boolean
    setOnlyShowLoaded: (onlyShowLoaded: boolean) => void
}

const ScriptMinimizeButton = ({minimize,onlyShowLoaded, setOnlyShowLoaded}: ScriptMinimizeButtonProps) => {

    return <>
        <div className="row text">
            <div className="col-lg-6">
                <div className="form-check form-switch">
                    <input className="form-check-input" type="checkbox" role="switch"
                           style={{lineHeight: "16px", marginTop: "2px"}}
                           checked={onlyShowLoaded}
                           onChange={() => setOnlyShowLoaded(!onlyShowLoaded)}
                    />
                    <label className="form-check-label" htmlFor="flexSwitchCheckDefault">Only show loaded</label>
                </div>
            </div>
        </div>
        <div className="scriptPanelButtonOuter">
            <div className="btn btn-primary btn-s scriptPanelButton" onClick={minimize}>
                <span className="glyphicon glyphicon-collapse-up scriptPanelButtonIcon"/>
            </div>
        </div>
    </>

}

interface ScriptsTableProps {
    scripts: Script[]
    hr?: React.JSX.Element
}

export const ScriptsTable = ({scripts, hr}: ScriptsTableProps) => {
    const scriptsLoading = useSelector((state: GmRootState) => state.users.edit.scriptsLoading)
    const sortedScripts = sortScripts(scripts)

    const rows = sortedScripts.map((script: Script) => {
        const loading = scriptsLoading.find((loading) => loading.scriptId === script.id)
        return <ScriptLine script={script}
                           loading={loading}
                           useCase={ScriptLineUseCase.HACKER}
                           key={script.id}/>
    })
    const rowTexts = sortedScripts.map((script: Script) => `${script.code}~${script.name}~${script.state}`)


    return <DataTable rows={rows} rowTexts={rowTexts} pageSize={35} hr={hr}>
        <div className="row text strong">
            <div className="col-lg-2 text-end">Code</div>
            <div className="col-lg-2">Name</div>
            <div className="col-lg-1">RAM</div>
            <div className="col-lg-2">State</div>
            <div className="col-lg-2">Action</div>
            <div className="col-lg-1">Effects</div>
        </div>
    </DataTable>
}

const sortScripts = (scripts: Script[]) => {
    const scriptsCopy = [...scripts]
    return scriptsCopy.sort(compareScriptStatus)
}

const compareScriptStatus = (a: Script, b: Script) => {
    if (a.state === b.state) return a.name.localeCompare(b.name)
    if (a.state === ScriptState.LOADED) return -1
    if (b.state === ScriptState.LOADED) return 1
    if (a.state === ScriptState.LOADING) return -1
    if (b.state === ScriptState.LOADING) return 1
    if (a.state === ScriptState.AVAILABLE) return -1
    if (b.state === ScriptState.AVAILABLE) return 1
    if (a.state === ScriptState.USED) return -1
    if (b.state === ScriptState.USED) return 1
    return a.name.localeCompare(b.name)
}

// const ScriptLine = ({script}: { script: Script }) => {
//
//     const scriptUsable = script.state !== ScriptState.USED && script.state !== ScriptState.EXPIRED
//     const codeElement = scriptUsable ? <td>
//         <SilentLink onClick={() => {
//             copyScript(script.code)
//         }}><>{script.code}</>
//         </SilentLink></td> : <td className="scriptUnusable">{script.code}</td>
//
//     const nameElement = scriptUsable ? <td className="text_light">{script.name}</td> : <td>{script.name}</td>
//
//
//     return <tr>
//         {codeElement}
//         {nameElement}
//         <td>
//             {script.effects.map((effect, index) => {
//                 const effectNumber = (index + 1).toString()
//                 return <><InfoBadge infoText={effect} key={effectNumber} badgeText={effectNumber}/>&nbsp;</>
//             })}
//         </td>
//         <td>
//             <ScriptStateBadge script={script} loading={null as unknown as ScriptLoading[]}/>
//         </td>
//     </tr>
// }



