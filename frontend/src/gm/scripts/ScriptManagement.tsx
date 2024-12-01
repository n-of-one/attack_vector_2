import {SilentLink} from "../../common/component/SilentLink";
import React from "react";
import {ObsoleteScriptType, scriptNames} from "../../common/script/ScriptModel";
import {webSocketConnection} from "../../common/server/WebSocketConnection";
import {useDispatch, useSelector} from "react-redux";
import {GmRootState} from "../GmRootReducer";
import {HackerRootState} from "../../hacker/HackerRootReducer";
import {navigateTo} from "../../common/menu/MenuItem";
import {Page} from "../../common/menu/pageReducer";
import {CloseButton} from "../../common/component/CloseButton";


export const ScriptManagementButton = () => {
    const currentPage = useSelector((state: HackerRootState) => state.currentPage)
    const dispatch = useDispatch()

    const navigateToManageScripts = () => {
        navigateTo(currentPage, Page.SCRIPT_MANAGEMENT, null, dispatch)
    }

    return <span className="btn btn-info button-text" onClick={navigateToManageScripts}>
            Current scripts
        </span>
}

interface ScriptEntry {
    type: ObsoleteScriptType,
    name: string,
}

export const ScriptManagement = () => {
    const user = useSelector((state: GmRootState) => state.users.edit.userData)
    const dispatch = useDispatch()

    if (!user) return <></>

    const addScript = (type: ObsoleteScriptType) => {
        webSocketConnection.send("/gm/script/add", {userId: user.id, type: type})
    }

    const scriptTypes = Object.values(ObsoleteScriptType);
    const scriptEntries: ScriptEntry[] = scriptTypes.map((scriptType: ObsoleteScriptType) => {
        return ({type: scriptType, name: scriptNames[scriptType]})
    })

    const backToUsers = () => {
        navigateTo(Page.SCRIPT_MANAGEMENT, Page.USERS, null, dispatch)
    }

    return (<>

            <div className="row">
                <div className="col-lg-2">
                </div>
                <div className="col-lg-4">
                    <div className="text">
                        <h3 className="text-info">Scripts of {user.name} </h3><br/>
                    </div>
                    <div className="d-flex flex-row justify-content-end"><CloseButton closeAction={backToUsers}/></div>
                    <div className="text">

                        <br/>
                        <br/>
                        {/*<ScriptsListObsolete user={user}/>*/}
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
                                        <tr key={user.id}>
                                            <td className="table-very-condensed"><SilentLink onClick={() => {
                                                addScript(entry.type)
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
        </>

    )

}
