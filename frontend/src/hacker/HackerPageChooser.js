import connect from "react-redux/es/connect/connect";
import {MAIL, SCAN} from "./HackerPages";
import MailHome from "./mail/MailHome";
import HackerHome from "./home/HackerHome";
import React from "react";
import ScanHome from "./scan/component/ScanHome";
import Terminal from "../common/terminal/Terminal";
import MenuBar from "../common/menu/MenuBar";

const mapDispatchToProps = (dispatch) => {
    return {
        dispatch: dispatch,
    };
};

let mapStateToProps = (state) => {
    return {
        messageTerminal: state.scan.messageTerminal,
        currentPage: state.currentPage
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


export default connect(mapStateToProps, mapDispatchToProps)(
    ({currentPage, messageTerminal, dispatch}) => {

        return (
            <span>
            <div className="container">
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
            </div>
            <MenuBar/>
        </span>);
    });

