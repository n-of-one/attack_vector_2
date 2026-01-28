import {RamDisplay} from "./RamDisplay";
import {ScriptsTable} from "../run/component/script/ScriptPanel";
import React from "react";
import {useSelector} from "react-redux";
import {HackerRootState} from "../HackerRootReducer";
import {Hr} from "../../common/component/dataTable/Hr";
import {Script, ScriptState} from "../../common/script/ScriptModel";
import {webSocketConnection} from "../../common/server/WebSocketConnection";
import {SilentLink} from "../../common/component/SilentLink";

export const HackerScriptsPanel = () => {
    const scriptStatus = useSelector((state: HackerRootState) => state.scriptStatus)
    const scripts = scriptStatus?.scripts || []
    const ram = scriptStatus?.ram || null

    return <>
        <RamDisplay size={509}/>
        <hr style={{color: "black"}}/>
        <div className="row text">
            <div className="col-lg-6">
                <strong>Scripts</strong>
            </div>
            <div className="col-lg-6">
                <DeleteUsedAndExpired scripts={scripts}/>
            </div>
        </div>
        <br/>
        <ScriptsTable scripts={scripts} hr={<Hr/>} ram={ram} showLoadButton={true}/>

    </>
}


const DeleteUsedAndExpired = ({scripts}: { scripts: Script[] }) => {
    const userOrExpired = scripts.filter(script => script.state === ScriptState.USED || script.state === ScriptState.EXPIRED)
    if (userOrExpired.length === 0) {
        return <></>
    }
    const cleanup = () => {
        webSocketConnection.send("/hacker/script/cleanup", null)
    }
    return <SilentLink onClick={cleanup}>
        <div className="btn btn-info" style={{fontSize: "12px"}}>Delete used and expired</div>
    </SilentLink>
}
