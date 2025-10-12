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
import {ScriptAccessManagement} from "./scripts/access/ScriptAccessManagement";
import {GmScriptHome} from "./scripts/GmScriptHome";
import {ScriptTypeManagement} from "./scripts/scriptType/ScriptTypeManagement";
import {CurrentScriptManagement} from "./scripts/currentScripts/CurrentScriptManagement";
import {GmStatisticsHome} from "./statistics/GmStatisticsHome";
import {ScriptIncome} from "./scripts/income/ScriptIncome";


const renderCurrentPage = (currentPage: Page) => {
    switch (currentPage) {
        case Page.SITES:
            return <SitesPage/>
        case Page.USERS:
            return <UserManagement/>
        case Page.GM_SCRIPTS_HOME:
            return <GmScriptHome/>
        case Page.SCRIPT_TYPE_MANAGEMENT:
            return <ScriptTypeManagement/>
        case Page.SCRIPT_MANAGEMENT:
            return <CurrentScriptManagement/>
        case Page.SCRIPT_ACCESS_MANAGEMENT:
            return <ScriptAccessManagement/>
        case Page.SCRIPT_INCOME_DATES:
            return <ScriptIncome/>
        case Page.TASKS:
            return <TaskMonitorHome/>
        case Page.STATISTICS:
            return <GmStatisticsHome/>
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
            <div className="content">
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
            </div>
            <MenuBar/>
        </div>
    )
}

