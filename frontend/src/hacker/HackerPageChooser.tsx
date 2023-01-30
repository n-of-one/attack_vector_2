import {MailHome} from "./mail/MailHome";
import {HackerHome} from "./home/HackerHome";
import React from "react";
import {Terminal} from "../common/terminal/Terminal";
import {MenuBar} from "../common/menu/MenuBar";
import runCanvas from "./run/component/RunCanvas";
import {RunPageChooser} from "./run/component/RunPageChooser";
import {useSelector} from "react-redux";
import {HackerState} from "./HackerRootReducer";
import {TerminalState} from "../common/terminal/TerminalReducer";
import {MAIL, SCAN} from "../common/menu/pageReducer";

const dismissScanInfo = (infoNodeId: string | null, event: any) => {
    if (!infoNodeId) return;

    let current = event.target;
    while (current) {
        if (current.id === "canvas-container" || current.id === "scanInfo") {
            return
        }
        current = current.parentElement;
    }

    runCanvas.unSelect()
};



const renderCurrentPage = (currentPage: string) => {
    switch (currentPage) {
        case MAIL:
            return <MailHome/>;
        case SCAN:
            return <RunPageChooser/>;
        default:
            return <HackerHome/>;
    }
}


export const HackerPageChooser = () => {

    const infoNodeId: string | null = useSelector((state: HackerState) => state.run?.infoNodeId)
    const messageTerminal: TerminalState = useSelector((state: HackerState) => state.run.messageTerminal)
    const currentPage: string =  useSelector((state: HackerState) =>  state.currentPage)

    return (
        <div className="container-fluid" data-bs-theme="dark" onClick={(event) => dismissScanInfo(infoNodeId, event)}>
            <div className="row">
                <div className="col-lg-2">
                    <div className="row">
                        <div className="col-lg-12">
                            <span className="text">&nbsp;</span>
                        </div>
                    </div>
                    <div className="row">
                        <div className="col-lg-12">
                            <Terminal terminal={messageTerminal} height="300px"/>
                        </div>
                    </div>
                </div>
                <div className="col-lg-10">
                    {renderCurrentPage(currentPage)}
                </div>
            </div>
            <MenuBar/>
        </div>
    );
}

