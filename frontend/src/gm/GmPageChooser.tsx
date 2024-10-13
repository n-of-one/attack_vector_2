import React from "react"
import {MenuBar} from "../common/menu/MenuBar"
import {useSelector} from "react-redux"
import {SitesPage} from "../common/sites/SitesPage";
import {UserManagement} from "../common/users/UserManagement";
import {GmRootState} from "./GmRootReducer";
import {SITES, TASKS, USERS} from "../common/menu/pageReducer";
import {TaskMonitorHome} from "./taskmonitor/TaskMonitorHome";


const renderCurrentPage = (currentPage: string) => {
    switch (currentPage) {
        case SITES:
            return <SitesPage/>
        case USERS:
            return <UserManagement/>
        case TASKS:
            return <TaskMonitorHome/>
        default:
            return <SitesPage/>
    }
}

export const GmPageChooser = () => {

    const currentPage: string = useSelector((state: GmRootState) => state.currentPage)

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

