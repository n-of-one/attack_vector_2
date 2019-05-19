import connect from "react-redux/es/connect/connect";
import {MAIL, SCAN} from "./HackerPages";
import MailHome from "./mail/MailHome";
import HackerHome from "./home/HackerHome";
import React from "react";
import ScanHome from "./scan/component/ScanHome";

const mapDispatchToProps = (dispatch) => {
    return {};
};

let mapStateToProps = (state) => {
    return {
        currentPage: state.currentPage
    };
};

export default connect(mapStateToProps, mapDispatchToProps)(
    ({currentPage}) => {
        switch(currentPage) {
            case MAIL: return <MailHome />;
            case SCAN: return <ScanHome />
            default: return <HackerHome />;
        }
    });
