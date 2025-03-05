import React, {useEffect} from "react";
import {navigateTo} from "../../common/menu/MenuItem";
import {Page} from "../../common/menu/pageReducer";
import {useDispatch, useSelector} from "react-redux";
import {ActionButton} from "../../common/component/ActionButton";
import {Icon} from "../../common/component/icon/Icon";
import {GLYPHICON_EXPAND, GLYPHICON_FLASH, GLYPHICON_LOGIN} from "../../common/component/icon/Glyphicon";
import {GmRootState} from "../GmRootReducer";
import {ScriptStatistics} from "./ScriptStatisticsReducer";
import {DataTable} from "../../common/component/dataTable/DataTable";
import {webSocketConnection} from "../../common/server/WebSocketConnection";
import {Hr} from "../../common/component/dataTable/Hr";


export const GmScriptHome = () => {
    const dispatch = useDispatch()
    // fetch the types, they are useful in all sub-pages
    useEffect(() => {
            webSocketConnection.send("/gm/scriptType/getAll", null)
        },
    )

    return (

        <div className="row">
            <div className="col-lg-1">
            </div>
            <div className="col-lg-5">
                <div className="text">
                    <h3 className="text-info">Script management</h3><br/>
                </div>
                <div className="text">
                    Scripts are single-use programs that hackers can use during a hack.<br/>
                    <br/>
                    <br/>
                    <br/>
                    <ActionButton onClick={() => {
                        navigateTo(Page.SCRIPT_MANAGEMENT, Page.SCRIPT_TYPE_MANAGEMENT, null, dispatch)
                    }}>
                        <><Icon type={GLYPHICON_FLASH} size="12px" height="12px"/> Script types</>
                    </ActionButton>
                    &nbsp;Manage the scripts that exist in your game.<br/>
                    <br/>
                    <br/>
                    <ActionButton onClick={() => {
                        navigateTo(Page.SCRIPT_MANAGEMENT, Page.SCRIPT_ACCESS_MANAGEMENT, null, dispatch)
                    }}>
                        <><Icon type={GLYPHICON_LOGIN} size="12px" height="12px"/> Script access</>
                    </ActionButton>
                    &nbsp;Manage the scripts that hackers have access to (without GM intervention)<br/>
                    <br/>
                    <br/>
                    <ActionButton onClick={() => {
                        navigateTo(Page.SCRIPT_MANAGEMENT, Page.SCRIPT_MANAGEMENT, null, dispatch)
                    }}>
                        <><Icon type={GLYPHICON_EXPAND} size="12px" height="12px"/> Current scripts</>
                    </ActionButton>
                    &nbsp; Directly manage the scripts hackers currently have.
                </div>


            </div>
            <div className="col-lg-6 rightPane rightPane">
                <div className="rightPanel">
                    <ScriptStatisticsPanel/>
                </div>
            </div>
        </div>
    )

}


const ScriptStatisticsPanel = () => {
    useEffect(() => {
        webSocketConnection.send("/gm/script/getStatistics", null)
    },[])

    const statisticsList = useSelector((state: GmRootState) => state.scriptsManagement.statistics)
    const sortedList = [...statisticsList].sort(sortStatistics)


    const rows = sortedList.map((line: ScriptStatistics, index: number) => {
            return (<div className="row text">
                    <div className="col-lg-2">{line.name}</div>
                    <div className="col-lg-2"><DarkZero value={line.owned}/></div>
                    <div className="col-lg-2"><DarkZero value={line.loaded}/></div>
                    <div className="col-lg-4"><DarkZero value={line.freeReceive}/></div>
                </div>
            )
        })
    const texts = sortedList.map(line => `${line.name}~${line.owned}~${line.loaded}~${line.freeReceive}`)
    const hr = <Hr height={4} marginTop={2}/>

    return (
        <DataTable rows={rows} rowTexts={texts} pageSize={35} hr={hr}>
            <div className="row text">
                <div className="col-lg-2">Type</div>
                <div className="col-lg-2">Owned</div>
                <div className="col-lg-2">Loaded</div>
                <div className="col-lg-4">Available per event for free</div>
            </div>
        </DataTable>
    )
}

const sortStatistics = (a: ScriptStatistics, b: ScriptStatistics) => {
    if (a.owned > 0 && b.owned === 0 ) return -1
    if (a.owned === 0 && b.owned > 0 ) return 1
    return a.name.localeCompare(b.name)
}

const DarkZero = ({value, text}: {value: number, text?: string}) => {
    const displayText = text ? text : value
    if (value === 0) return <span className="dark">{displayText}</span>
    return <>{displayText}</>
}
