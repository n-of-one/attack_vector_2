import {Page} from "../common/menu/pageReducer";
import {UserManagement} from "../common/users/UserManagement";
import {TaskMonitorHome} from "../gm/taskmonitor/TaskMonitorHome";
import {useSelector} from "react-redux";
import {GmRootState} from "../gm/GmRootReducer";
import {MenuBar} from "../common/menu/MenuBar";
import React from "react";
import {ConfigHome} from "./config/ConfigHome";
import {UserType} from "../common/users/CurrentUserReducer";
import {HackerRootState} from "../hacker/HackerRootReducer";

const renderCurrentPage = (currentPage: Page) => {
    switch (currentPage) {
        case Page.USERS:
            return <UserManagement/>
        case Page.TASKS:
            return <TaskMonitorHome/>
        case Page.CONFIG:
            return <ConfigHome/>
        default:
            return <ConfigHome/>
    }
}

export const AdminPageChooser = () => {

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
