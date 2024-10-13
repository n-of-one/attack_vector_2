import {HackerHome} from "./home/HackerHome"
import React from "react"
import {MenuBar} from "../common/menu/MenuBar"
import {runCanvas} from "./run/component/RunCanvas"
import {useSelector} from "react-redux"
import {HackerRootState} from "./HackerRootReducer"
import {FORCE_DISCONNECT, ME, RUN, SITES, USERS} from "../common/menu/pageReducer"
import {RunHome} from "./run/component/RunHome";
import {UserManagement} from "../common/users/UserManagement";
import {ForceDisconnected} from "../common/component/ForceDisconnected";
import {Me} from "../common/users/Me";
import {SitesPage} from "../common/sites/SitesPage";

const dismissScanInfo = (infoNodeId: string | null, event: any) => {
    if (!infoNodeId) return

    let current = event.target
    while (current) {
        if (current.id === "canvas-container" || current.id === "scanInfo") {
            return
        }
        current = current.parentElement
    }

    runCanvas.unSelect()
}



const renderCurrentPage = (currentPage: string) => {
    switch (currentPage) {
        case RUN:
            return <RunHome/>
        case SITES:
            return <SitesPage/>
        case ME:
            return <Me/>
        case USERS:
            return <UserManagement/>
        default:
            return <HackerHome/>
    }
}


export const HackerPageChooser = () => {

    const infoNodeId: string | null = useSelector((state: HackerRootState) => state.run?.infoNodeId)

    const currentPage: string = useSelector((state: HackerRootState) => state.currentPage)
    if (currentPage === FORCE_DISCONNECT) return <ForceDisconnected/>

    return (
        <div className="container-fluid" data-bs-theme="dark" onClick={(event) => dismissScanInfo(infoNodeId, event)}>
            {renderCurrentPage(currentPage)}
            <MenuBar/>
        </div>
    )
}

