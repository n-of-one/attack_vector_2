import React from "react"
import {MenuBar} from "../common/menu/MenuBar"
import {useSelector} from "react-redux"
import {GmSites} from "./sites/GmSites";
import {UserManagement} from "../common/users/UserManagement";
import {GmState} from "./GmRootReducer";
import {ADMIN, GM_SITES, LOGS, MISSIONS, USERS} from "../common/menu/pageReducer";



const renderCurrentPage = (currentPage: string) => {
    switch (currentPage) {
        case GM_SITES:
            return <GmSites/>
        case USERS:
            return <UserManagement/>
        case LOGS:
            return <GmSites/>
        case MISSIONS:
            return <GmSites/>
        case ADMIN:
            return <GmSites/>
        default:
            return <GmSites/>
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

