import React from "react"
import {MenuBar} from "../common/menu/MenuBar"
import {useSelector} from "react-redux"
import {SitesPage} from "../common/sites/SitesPage";
import {UserManagement} from "../common/users/UserManagement";
import {GmRootState} from "./GmRootReducer";
import {TaskMonitorHome} from "./taskmonitor/TaskMonitorHome";
import {UserType} from "../common/users/CurrentUserReducer";
import {HackerRootState} from "../hacker/HackerRootReducer";
import {Page} from "../common/menu/pageReducer";
import {ScriptManagement} from "./scripts/ScriptManagement";
import {ScriptAccessManagement} from "./scripts/ScriptAccessManagement";
import {GmScriptHome} from "./scripts/GmScriptHome";
import {ScriptTypeOverview} from "./scripts/scriptType/ScriptTypeOverview";


const renderCurrentPage = (currentPage: Page) => {
    switch (currentPage) {
        case Page.SITES:
            return <SitesPage/>
        case Page.USERS:
            return <UserManagement/>
        case Page.GM_SCRIPTS_HOME:
            return <GmScriptHome/>
        case Page.SCRIPT_TYPE_MANAGEMENT:
            return <ScriptTypeOverview/>
        case Page.SCRIPT_MANAGEMENT:
            return <ScriptManagement/>
        case Page.SCRIPT_ACCESS_MANAGEMENT:
            return <ScriptAccessManagement/>
        case Page.TASKS:
            return <TaskMonitorHome/>
        default:
            return <SitesPage/>
    }
}

export const GmPageChooser = () => {

    const currentPage: Page = useSelector((state: GmRootState) => state.currentPage)
    const currentUser = useSelector((state: HackerRootState) => state.currentUser)

    if (currentUser.type === UserType.NO_DATA) return <></> // waiting for currentUser to be received

    return (
        <div className="container-fluid" data-bs-theme="dark">
            <div className="row">
                <div className="col-lg-2">
                    <span className="text">&nbsp;</span>
                </div>
                <div className="col-lg-5 backgroundLight">
                    <span className="text">&nbsp;</span>
                </div>
                <div className="col-lg-5 rightPane">
                    <span className="text">&nbsp;</span>
                </div>
            </div>
            {renderCurrentPage(currentPage)}
            <MenuBar/>
        </div>
    )
}

