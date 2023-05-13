import React from "react"
import {Terminal} from "../common/terminal/Terminal"
import {MenuBar} from "../common/menu/MenuBar"
import {useSelector} from "react-redux"
import {GmHome} from "./component/GmHome";
import {UserManagement} from "./users/UserManagement";
import {GmState} from "./GmRootReducer";
import {UserDetails} from "./users/UserDetails";
import {ADMIN, GM_SITES, LOGS, MISSIONS, USERS} from "../common/menu/pageReducerX";
import {RequiresRole} from "../common/RequiresRole";
import {ROLE_USER_MANAGER} from "../common/UserAuthorizations";



const renderCurrentPage = (currentPage: string) => {
    switch (currentPage) {
        case GM_SITES:
            return <GmHome/>
        case USERS:
            return <UserManagement/>
        case LOGS:
            return <GmHome/>
        case MISSIONS:
            return <GmHome/>
        case ADMIN:
            return <GmHome/>
        default:
            return <GmHome/>
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

