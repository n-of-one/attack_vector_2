import React, {useState} from "react";
import {useSelector} from "react-redux";
import {HackerRootState} from "../../../HackerRootReducer";
import {Script, ScriptState} from "../../../../common/script/ScriptModel";
import {ScriptLine, ScriptLineUseCase} from "../../../../common/script/ScriptLine";
import {DataTable} from "../../../../common/component/dataTable/DataTable";
import {Hr} from "../../../../common/component/dataTable/Hr";
import {RamBar} from "../../../scripts/RamDisplay";
import {Ram} from "../../../../common/script/ScriptStatusReducer";
import {formatTimeInterval} from "../../../../common/util/Util";

export const ScriptPanel = () => {
    const [expanded, setExpanded] = React.useState(false)
    const scriptStatus = useSelector((state: HackerRootState) => state.scriptStatus)
    const [onlyShowLoaded, setOnlyShowLoaded] = useState(true)

    if (!scriptStatus) {
        return <></>
    }

    const scripts = scriptStatus?.scripts

    const expand = () => {
        setExpanded(true)
    }
    const minimize = () => {
        setExpanded(false)
    }


    const filteredScripts = onlyShowLoaded ? scripts.filter((script: Script) => script.state === ScriptState.LOADED) : scripts

    const hr = <Hr color="#666"/>

    if (expanded) {
        return <div className="scriptPanel" style={{width: "780px"}}>
            <ScriptPanelExpandedHead minimize={minimize} onlyShowLoaded={onlyShowLoaded} setOnlyShowLoaded={setOnlyShowLoaded} ram={scriptStatus.ram}/>
            <ScriptsTable scripts={filteredScripts} hr={hr} minimize={minimize} ram={scriptStatus.ram} shownInRun={true}/>
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
    ram: Ram
}

const ScriptPanelExpandedHead = ({minimize, onlyShowLoaded, setOnlyShowLoaded, ram}: ScriptMinimizeButtonProps) => {

    const refreshText = ram.nextRefreshSecondsLeft === null ? "" : `(${formatTimeInterval(ram.nextRefreshSecondsLeft)})`

    return <>
        <div className="row text">
            <div className="col-lg-3">
                <div className="form-check form-switch">
                    <input className="form-check-input" type="checkbox" role="switch"
                           style={{lineHeight: "16px", marginTop: "2px"}}
                           checked={onlyShowLoaded}
                           onChange={() => setOnlyShowLoaded(!onlyShowLoaded)}
                    />
                    <label className="form-check-label" htmlFor="flexSwitchCheckDefault">Only show loaded</label>
                </div>
            </div>
            <div className="col-lg-1" style={{"textAlign": "right", "paddingRight": 0}}>
                RAM
            </div>
            <div className="col-lg-6">
                <RamBar ram={ram} size={368}/>
            </div>
            <div className="col-lg-2" style={{paddingLeft: 0}}>{refreshText}</div>
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
    minimize?: () => void
    ram: Ram | null
    shownInRun: boolean
}

export const ScriptsTable = ({scripts, hr, minimize, ram, shownInRun}: ScriptsTableProps) => {
    if (!ram) return <></>

    const sortedScripts = sortScripts(scripts)

    const rows = sortedScripts.map((script: Script) => {
        return <ScriptLine script={script}
                           useCase={ScriptLineUseCase.HACKER}
                           key={script.id}
                           minimize={minimize}
                           ram={ram}
                           shownInRun={shownInRun}
        />
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
    if (a.state === ScriptState.AVAILABLE) return -1
    if (b.state === ScriptState.AVAILABLE) return 1
    if (a.state === ScriptState.USED) return -1
    if (b.state === ScriptState.USED) return 1
    return a.name.localeCompare(b.name)
}
