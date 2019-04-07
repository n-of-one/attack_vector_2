import React from 'react';
import {connect} from "react-redux";
import Cookies from "js-cookie";
import {NAVIGATE_PAGE} from "../CommonActions";

/* eslint jsx-a11y/anchor-is-valid: 0*/

const mapDispatchToProps = (dispatch) => {
    return {
        navigateTo: (event, targetPage) => {
            event.preventDefault();
            console.log("Navigating to page: " + targetPage);
            dispatch({type: NAVIGATE_PAGE, target: targetPage }); return false; }
    }
};
let mapStateToProps = (state) => {

    let userName = Cookies.get("userName");
    let roles = Cookies.get("roles").split("|");

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
                return <li className="active"><a href="#" onClick={(e) => { e.preventDefault();}}>{label}</a></li>
            }
            else {
                return <li><a href="#" onClick={(e) => navigateTo(e, targetPage)}>{label}</a></li>
            }
        }
        else {
            return <span >not showing {label} </span>
        }

    });
