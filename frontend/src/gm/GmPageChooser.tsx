import React from "react"
import {MenuBar} from "../common/menu/MenuBar"
import {useSelector} from "react-redux"
import {GmSitesHome} from "./sites/GmSitesHome";
import {UserManagement} from "../common/users/UserManagement";
import {GmState} from "./GmRootReducer";
import {ADMIN, GM_SITES, LOGS, MISSIONS, TASKS, USERS} from "../common/menu/pageReducer";
import {TaskMonitorHome} from "./taskmonitor/TaskMonitorHome";


const renderCurrentPage = (currentPage: string) => {
    switch (currentPage) {
        case GM_SITES:
            return <GmSitesHome/>
        case USERS:
            return <UserManagement/>
        case LOGS:
            return <GmSitesHome/>
        case MISSIONS:
            return <GmSitesHome/>
        case TASKS:
            return <TaskMonitorHome/>
        case ADMIN:
            return <GmSitesHome/>
        default:
            return <GmSitesHome/>
    }
}

export const GmPageChooser = () => {

    const currentPage: string =  useSelector((state: GmState) =>  state.currentPage)

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

