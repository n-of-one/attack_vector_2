import {MailHome} from "./mail/MailHome"
import {HackerHome} from "./home/HackerHome"
import React from "react"
import {Terminal} from "../common/terminal/Terminal"
import {MenuBar} from "../common/menu/MenuBar"
import {runCanvas} from "./run/component/RunCanvas"
import {useSelector} from "react-redux"
import {HackerState} from "./HackerRootReducer"
import {TerminalState} from "../common/terminal/TerminalReducer"
import {FORCE_DISCONNECT, MAIL, ME, RUN, USERS} from "../common/menu/pageReducer"
import {RunHome} from "./run/component/RunHome";
import {UserManagement} from "../common/users/UserManagement";
import {ForceDisconnected} from "../common/component/ForceDisconnected";
import {Me} from "../common/users/Me";

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
        case MAIL:
            return <MailHome/>
        case RUN:
            return <RunHome/>
        case ME:
            return <Me/>
        case USERS:
            return <UserManagement/>
        default:
            return <HackerHome/>
    }
}


export const HackerPageChooser = () => {

    const infoNodeId: string | null = useSelector((state: HackerState) => state.run?.infoNodeId)
    const messageTerminal: TerminalState = useSelector((state: HackerState) => state.run.messageTerminal)

    const currentPage: string =  useSelector((state: HackerState) =>  state.currentPage)
    if (currentPage === FORCE_DISCONNECT) return <ForceDisconnected/>

    return (
        <div className="container-fluid" data-bs-theme="dark" onClick={(event) => dismissScanInfo(infoNodeId, event)}>
            {renderCurrentPage(currentPage)}
            <MenuBar/>
        </div>
    )
}

