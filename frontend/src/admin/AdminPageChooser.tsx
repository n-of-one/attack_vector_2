import {CONFIG, SITES, TASKS, USERS} from "../common/menu/pageReducer";
import {SitesPage} from "../common/sites/SitesPage";
import {UserManagement} from "../common/users/UserManagement";
import {TaskMonitorHome} from "../gm/taskmonitor/TaskMonitorHome";
import {useSelector} from "react-redux";
import {GmState} from "../gm/GmRootReducer";
import {MenuBar} from "../common/menu/MenuBar";
import React from "react";
import {ConfigHome} from "./config/ConfigHome";

const renderCurrentPage = (currentPage: string) => {
    switch (currentPage) {
        case TASKS:
            return <TaskMonitorHome/>
        case CONFIG:
            return <ConfigHome/>
        default:
            return <ConfigHome/>
    }
}

export const AdminPageChooser = () => {

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
