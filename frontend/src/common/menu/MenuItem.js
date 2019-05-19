import React from 'react';
import {connect} from "react-redux";
import Cookies from "js-cookie";
import {NAVIGATE_PAGE} from "../enums/CommonActions";
import SilentLink from "../component/SilentLink";

/* eslint jsx-a11y/anchor-is-valid: 0*/

const mapDispatchToProps = (dispatch) => {
    return {
        navigateTo: (event, targetPage, currentPage) => {
            event.preventDefault();
            console.log("Navigating to page: " + targetPage);
            dispatch({type: NAVIGATE_PAGE, to: targetPage, from: currentPage }); return false; }
    }
};
let mapStateToProps = (state) => {

    let userName = Cookies.get("userName");
    let rolesValue = Cookies.get("roles");
    let roles = (rolesValue) ? rolesValue.split("|") : [];

    return {
        roles: roles,
        userName: userName,
        currentPage: state.currentPage
    };
};

export default connect(mapStateToProps, mapDispatchToProps)(
    ({roles, userName, currentPage,
         requriesRole, targetPage, label,
         navigateTo}) => {


        if (roles.includes(requriesRole)) {
            if (currentPage === targetPage) {
                return <li className="active"><SilentLink onClick={(e) => { e.preventDefault();}}>{label}</SilentLink></li>
            }
            else {
                return <li><SilentLink onClick={(e) => navigateTo(e, targetPage, currentPage)}>{label}</SilentLink></li>
            }
        }
        else {
            return <span>{label} </span>
        }

    });
