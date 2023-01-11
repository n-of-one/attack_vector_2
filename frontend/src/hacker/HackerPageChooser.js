import connect from "react-redux/es/connect/connect";
import {MAIL, SCAN} from "./HackerPages";
import MailHome from "./mail/MailHome";
import HackerHome from "./home/HackerHome";
import React from "react";
import Terminal from "../common/terminal/Terminal";
import MenuBar from "../common/menu/MenuBar";
import runCanvas from "./run/component/RunCanvas";
import RunPageChooser from "./run/component/RunPageChooser";

const dismissScanInfo = (renderScanInfoDismiss, event) => {
    if (!renderScanInfoDismiss) return;

    let current = event.target;
    while (current) {
        if (current.id === "canvas-container" || current.id === "scanInfo") {
            return
        }
        current = current.parentElement;
    }

    runCanvas.unSelect()
};

const mapDispatchToProps = (dispatch) => {
    return {
        dismissScanInfo: dismissScanInfo,
    };
};

let mapStateToProps = (state) => {
    const scanInfoDismiss = !!(state.run && state.run.infoNodeId);
    return {
        messageTerminal: state.run.messageTerminal,
        currentPage: state.currentPage,
        renderScanInfoDismiss: scanInfoDismiss,
    };
};


const renderCurrentPage = (currentPage) => {

    switch (currentPage) {
        case MAIL:
            return <MailHome/>;
        case SCAN:
            return <RunPageChooser/>;
        default:
            return <HackerHome/>;
    }
};

export default connect(mapStateToProps, mapDispatchToProps)(
    ({currentPage, messageTerminal, dispatch, renderScanInfoDismiss, dismissScanInfo}) => {

        return (
            <div className="container-fluid" data-bs-theme="dark" onClick={(event) => dismissScanInfo(renderScanInfoDismiss, event)}>
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
    });

