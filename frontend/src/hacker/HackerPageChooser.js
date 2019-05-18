import connect from "react-redux/es/connect/connect";
import {MAIL} from "./HackerPages";
import MailHome from "./mail/component/MailHome";
import HackerHome from "./component/HackerHome";
import React from "react";

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
            default: return <HackerHome />;
        }
    });
