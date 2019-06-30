import connect from "react-redux/es/connect/connect";
import {MAIL, SCAN} from "./HackerPages";
import MailHome from "./mail/MailHome";
import HackerHome from "./home/HackerHome";
import React from "react";
import ScanHome from "./run/component/RunHome";
import Terminal from "../common/terminal/Terminal";
import MenuBar from "../common/menu/MenuBar";
import runCanvas from "./run/component/RunCanvas";

const dismissScanInfo = (event) => {
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
        dispatch: dispatch,
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
            return <ScanHome/>;
        default:
            return <HackerHome/>;
    }
};

const renderMain = (currentPage, messageTerminal, dispatch) => {
    return (
        <>
            <div className="row">
                <div className="col-lg-2">
                    <div className="row">
                        <div className="col-lg-12">
                            <span className="text">&nbsp;</span>
                        </div>
                    </div>
                    <div className="row">
                        <div className="col-lg-12">
                            <Terminal terminal={messageTerminal} dispatch={dispatch} height="300px"/>
                        </div>
                    </div>
                </div>
                <div className="col-lg-10">
                    {renderCurrentPage(currentPage)}
                </div>
            </div>
            <MenuBar/>
        </>
    );
}

export default connect(mapStateToProps, mapDispatchToProps)(
    ({currentPage, messageTerminal, dispatch, renderScanInfoDismiss, dismissScanInfo}) => {

        if (renderScanInfoDismiss) {
            return <div className="container" onClick={(event) => dismissScanInfo(event)}>
                {renderMain(currentPage, messageTerminal, dispatch)}
            </div>;
        }
        return <div className="container">
            {renderMain(currentPage, messageTerminal, dispatch)}
        </div>;
    });

