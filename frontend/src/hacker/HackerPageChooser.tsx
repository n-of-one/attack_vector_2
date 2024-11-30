import {HackerHome} from "./home/HackerHome"
import React from "react"
import {MenuBar} from "../common/menu/MenuBar"
import {runCanvas} from "./run/component/RunCanvas"
import {useSelector} from "react-redux"
import {HackerRootState} from "./HackerRootReducer"
import {RunHome} from "./run/component/RunHome";
import {ForceDisconnected} from "../common/component/ForceDisconnected";
import {Me} from "../common/users/Me";
import {SitesPage} from "../common/sites/SitesPage";
import {UserType} from "../common/users/CurrentUserReducer";
import {Page} from "../common/menu/pageReducer";
import {HackerScriptsHome} from "./scripts/HackerScriptsHome";

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


const renderCurrentPage = (currentPage: Page) => {
    switch (currentPage) {
        case Page.RUN:
            return <RunHome/>
        case Page.SITES:
            return <SitesPage/>
        case Page.HACKER_SCRIPTS:
            return <HackerScriptsHome />
        case Page.ME:
            return <Me/>
        default:
            return <HackerHome/>
    }
}


export const HackerPageChooser = () => {

    const currentUser = useSelector((state: HackerRootState) => state.currentUser)
    const infoNodeId: string | null = useSelector((state: HackerRootState) => state.run?.infoNodeId)
    const currentPage: Page = useSelector((state: HackerRootState) => state.currentPage)

    if (currentPage === Page.FORCE_DISCONNECT) return <ForceDisconnected/>
    if (currentUser.type === UserType.NO_DATA) return <></> // waiting for currentUser to be received

    return (
        <div className="container-fluid" data-bs-theme="dark" onClick={(event) => dismissScanInfo(infoNodeId, event)}>
            {renderCurrentPage(currentPage)}
            <MenuBar/>
        </div>
    )
}

