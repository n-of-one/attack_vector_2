import React, {useEffect} from "react";
import {useSelector} from "react-redux";
import {HackerRootState} from "../HackerRootReducer";
import {SilentLink} from "../../common/component/SilentLink";
import {webSocketConnection} from "../../common/server/WebSocketConnection";
import {ScriptAccess} from "../../gm/scripts/access/ScriptAccessReducer";
import {InfoBadge} from "../../common/component/ToolTip";
import {ScriptsTable} from "../run/component/script/ScriptPanel";
import {Script, ScriptState} from "../../common/script/ScriptModel";
import {Hr} from "../../common/component/dataTable/Hr";


export const HackerScriptsHome = () => {
    const currentUser = useSelector((state: HackerRootState) => state.currentUser)
    const scripts = currentUser.hacker?.scripts || []

    useEffect(() => {
        webSocketConnection.send("/hacker/scriptAccess/get", currentUser.id)
    }, [scripts, currentUser.id])


    const accesses = useSelector((state: HackerRootState) => state.scriptAccess)

const hr= <Hr />

    return (
        <div className="row">
            <div className="col-lg-1">
            </div>
            <div className="col-lg-4 text">
                <br/>
                <strong>🜁 Verdant OS 🜃</strong><br/>
                <br/>
                Scripts exploit security flaws, but those flaws will be patched automatically by security systems after use,
                so all script are use once. Scripts can only be used on the day they are created. The daily patching cycle runs at 06:00.<br/>
                <br/>
                <hr/>
                You have 12 RAM slots available for scripts. Loading a script takes 30 minutes.<br/>
                <br/>
                <FreeReceive accesses={accesses}/>
                <br/>
                <br/>
                <br/>
                <ShopForScripts accesses={accesses}/>

            </div>
            <div className="col-lg-7">
                <div className="rightPanel">
                    <div className="row text">
                        <div className="col-lg-6">
                            <strong>Scripts</strong>
                        </div>
                        <div className="col-lg-6">
                            <DeleteUsedAndExpired scripts={scripts}/>
                        </div>
                    </div>
                    <br/>
                    <ScriptsTable scripts={scripts} hr={hr}/>
                    <br/>

                </div>

            </div>
        </div>
    )
}

const DeleteUsedAndExpired = ({scripts}:{scripts: Script[]}) => {
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


const FreeReceive = ({accesses}: { accesses: ScriptAccess[] }) => {
    const accessWithFreeReceive = accesses.filter(access => access.receiveForFree > 0)

    const retrieve = () => {
        webSocketConnection.send("/hacker/script/freeReceive", null)
    }


    if (accessWithFreeReceive.length === 0) {
        return <></>
    }
    return (<>
            <hr/>
            You have access to the following scripts for free:<br/><br/>
            <div className="row">
                <div className="col-lg-offset-2 col-lg-4">Script</div>
                <div className="col-lg-4">Effects</div>
                <div className="col-lg-2">Status</div>
            </div>
            <hr style={{margin: "4px 0 4px 0", color: "#555", borderTop: "1px dashed"}}/>
            {
                accessWithFreeReceive.map((access: ScriptAccess) => {

                    return (
                        <div className="row" key={access.id}>
                            <div className="col-lg-offset-2 col-lg-4">{access.receiveForFree}x {access.type.name}</div>
                            <div className="col-lg-4">{
                                access.type.effects.map((effect: string, index: number) => {
                                    return (<>
                                        <InfoBadge infoText={effect} key={index}
                                                   badgeText={(index + 1).toString()}/>
                                        &nbsp;</>)
                                })
                            }
                            </div>
                            <div className="col-lg-2"><ScriptAccessStatus access={access}/></div>
                            <hr style={{margin: "4px 0 4px 0", color: "#555", borderTop: "1px dashed"}}/>
                        </div>)
                })
            }
            <br/>
            <br/>
            <br/>
            <SilentLink onClick={retrieve}>
                <div className="btn btn-info" style={{fontSize: "12px"}}>Retrieve scripts</div>
            </SilentLink>
            <br/>
        </>
    )
}

const ScriptAccessStatus = ({access}: { access: ScriptAccess }) => {
    if (access.used) {
        return <span className="badge bg-secondary">Already received</span>
    }
    return <span className="badge bg-primary">Available</span>

}

const ShopForScripts = ({accesses}: { accesses: ScriptAccess[] }) => {
    const accessWithPrice = accesses.filter(access => access.price !== null)

    const openShop = () => {
        webSocketConnection.send("/hacker/scriptAccess/get", null)
    }


    if (accessWithPrice.length === 0) {
        return <></>
    }
    return (<>
        <hr/>
        You can also purchase scripts on the dark web. <SilentLink onClick={openShop}>
        <div className="btn btn-info" style={{fontSize: "12px"}}>Visit script store</div>
    </SilentLink>

    </>)
}






