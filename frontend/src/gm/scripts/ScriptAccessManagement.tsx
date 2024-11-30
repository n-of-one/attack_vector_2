import {SilentLink} from "../../common/component/SilentLink";
import React from "react";
import {scriptNames, ObsoleteScriptType} from "../../common/script/ScriptModel";
import {webSocketConnection} from "../../common/server/WebSocketConnection";
import {useDispatch, useSelector} from "react-redux";
import {GmRootState} from "../GmRootReducer";
import {HackerRootState} from "../../hacker/HackerRootReducer";
import {navigateTo} from "../../common/menu/MenuItem";
import {Page} from "../../common/menu/pageReducer";
import {HackerScriptAccess} from "../../common/script/ScriptAccessModel";
import {TextSaveInput} from "../../common/component/TextSaveInput";
import {User} from "../../common/users/CurrentUserReducer";
import {CloseButton} from "../../common/component/CloseButton";
import {ToolTip} from "../../common/component/ToolTip";


export const ScriptAccessManagementButton = () => {
    const currentPage = useSelector((state: HackerRootState) => state.currentPage)
    const dispatch = useDispatch()

    const navigateToManageScripts = () => {
        navigateTo(currentPage, Page.SCRIPT_ACCESS_MANAGEMENT, null, dispatch)
    }

    return <span className="btn btn-info button-text" onClick={navigateToManageScripts}>
            Script access
        </span>
}

interface ScriptEntry {
    type: ObsoleteScriptType,
    name: string,
}

export const ScriptAccessManagement = () => {
    const user = useSelector((state: GmRootState) => state.users.edit)
    const dispatch = useDispatch()
    if (!user) return <></>


    const addAccess = (type: ObsoleteScriptType) => {
        webSocketConnection.send("/gm/scriptAccess/add", {userId: user.id, type: type})
    }

    const scriptTypes = Object.values(ObsoleteScriptType);
    const scriptEntries: ScriptEntry[] = scriptTypes.map((scriptType: ObsoleteScriptType) => {
        return ({type: scriptType, name: scriptNames[scriptType]})
    })

    const backToUsers = () => {
        navigateTo(Page.SCRIPT_MANAGEMENT, Page.USERS, null, dispatch)
    }

    return (

        <div className="row">
            <div className="col-lg-1">
            </div>
            <div className="col-lg-5">
                <div className="text">
                    <h3 className="text-info">{user.name} - access to scripts </h3><br/>
                </div>
                <div className="d-flex flex-row justify-content-end"><CloseButton closeAction={backToUsers}/></div>
                <div className="text">
                    <br/>
                    <div className="row form-group">
                        <div className="col-lg-1" style={{paddingTop: "7px", marginRight: "-30px"}}></div>
                        <div className="col-lg-4"><br/>Name</div>
                        <div className="col-lg-2"><br/>Value</div>
                        <div className="col-lg-1"><br/>RAM</div>
                        <div className="col-lg-1">Free<br/>receive</div>
                        <div className="col-lg-1"><br/>Price</div>
                        <div className="col-lg-1"><br/>Action</div>
                    </div>
                    <br/>


                    <ScriptAccessList user={user}/>
                </div>

            </div>
            <div className="col-lg-6 rightPane rightPane">
        <div className="siteMap">
                    <table className="table table-sm text-muted text" id="sitesTable">
                        <thead>
                        <tr>
                            <td className="strong">Name</td>
                            <td className="strong">Character</td>
                        </tr>
                        </thead>
                        <tbody>
                        {
                            scriptEntries.map((entry: ScriptEntry) => {
                                return (
                                    <tr key={entry.type}>
                                        <td className="table-very-condensed"><SilentLink onClick={() => {
                                            addAccess(entry.type)
                                        }}><>{entry.name}</>
                                        </SilentLink>
                                        </td>
                                    </tr>)
                            })
                        }
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    )

}

const ScriptAccessList = ({user}: { user: User }) => {
    const access = user.hacker?.scriptAccess || []
    return <>{access.map(access => <ScriptAccessElement access={access} key={access.id}/>)}</>
}


const ScriptAccessElement = ({access}: { access: HackerScriptAccess }) => {

    const save = (toSave: HackerScriptAccess) => {
        webSocketConnection.send("/gm/scriptAccess/edit", toSave)
    }

    const deleteAccess = () => {
        webSocketConnection.send("/gm/scriptAccess/delete", access.id)
    }

    return <div className="row form-group">
        {/*<label htmlFor={access.id} className="col-lg-4 control-label text-muted">{label}</label>*/}
        <div className="col-lg-1" style={{paddingTop: "7px", marginRight: "-30px"}}>
            <ToolTip text={access.type} id={access.id}>
                <span className="badge bg-secondary helpBadge">?</span>
            </ToolTip>
        </div>

        <div className={`col-lg-4`}>
            <TextSaveInput id={access.id} className="form-control"
                           placeholder="" value={access.name}
                           save={(name) => {
                               const accessCopy = {...access, name}
                               save(accessCopy)
                           }}
                           readonly={false}/>
        </div>
        <div className={`col-lg-2`}>
            <TextSaveInput id={access.id} className="form-control"
                           placeholder="" value={access.value || ""}
                           save={(value) => {
                               const accessCopy = {...access, value}
                               save(accessCopy)
                           }}
                           readonly={false}/>
        </div>
        <div className="col-lg-1">
            <TextSaveInput id={access.id} className="form-control"
                           placeholder="" value={access.ram}
                           save={(ramInput) => {
                               const ram = isNaN(parseInt(ramInput)) ? 0 : parseInt(ramInput)
                               const accessCopy = {...access, ram}
                               save(accessCopy)
                           }}
                           readonly={false}/>
        </div>
        <div className="col-lg-1">
            <TextSaveInput id={access.id} className="form-control"
                           placeholder="" value={`${access.receiveForFree}x`}
                           save={(usesInput) => {
                               const normalized = usesInput.replace("x", "")
                               const receiveForFree = isNaN(parseInt(normalized)) ? 0 : parseInt(normalized)
                               const accessCopy = {...access, receiveForFree}
                               save(accessCopy)
                           }}
                           readonly={false}/>
        </div>
        <div className="col-lg-1">
            <TextSaveInput id={access.id} className="form-control"
                           placeholder="" value={access.price === null ? "" : access.price}
                           save={(priceInput) => {
                               const price = isNaN(parseFloat(priceInput)) ? null : parseFloat(priceInput)
                               const accessCopy = {...access, price}
                               save(accessCopy)
                           }}
                           readonly={false}/>
        </div>
        <div className="col-lg-1" style={{paddingTop: "9px"}}>
            <SilentLink onClick={deleteAccess} title="Remove script">
                <span className="glyphicon glyphicon-trash"/>
            </SilentLink>
        </div>
    </div>

}
