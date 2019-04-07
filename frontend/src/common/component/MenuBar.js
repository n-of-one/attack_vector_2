import React from 'react';
import {connect} from "react-redux";
import Cookies from "js-cookie";
import MenuItem from "./MenuItem";
import {ADMIN, GM_SITES, LOGS, MISSIONS, USERS} from "../../gm/GmPages";
import {HACKER_COMMUNITY, HACKER_HOME, SCRIPTS} from "../../hacker/HackerPages";
import {ME} from "../CommonPages";

/* eslint jsx-a11y/anchor-is-valid: 0*/

const mapDispatchToProps = (dispatch) => {
    return {}
};
let mapStateToProps = (state) => {

    let userName = Cookies.get("userName");

    return {
        userName: userName,
        currentPage: state.currentPage
    };
};

let logout = (event) => {
    event.preventDefault();
    Cookies.remove("jwt");
    Cookies.remove("type");
    Cookies.remove("roles");
    Cookies.remove("userName");
    document.location.href = "/login";
};

export default connect(mapStateToProps, mapDispatchToProps)(
    ({userName}) => {

        return (
            <div className="navbar navbar-inverse navbar-fixed-bottom">
                <div className="container">
                    <div className="navbar-header">
                        <button type="button" className="navbar-toggle" data-toggle="collapse"
                                data-target=".nav-collapse">
                            <span className="icon-bar"/>
                            <span className="icon-bar"/>
                            <span className="icon-bar"/>
                        </button>
                        <a className="navbar-brand">↼ Attack Vector ⇁</a>
                    </div>
                    <div className="navbar-collapse collapse">
                        <ul className="nav navbar-nav">
                            <MenuItem requriesRole="ROLE_HACKER" targetPage={SCRIPTS} label="Scripts" />
                            <MenuItem requriesRole="ROLE_HACKER" targetPage={HACKER_HOME} label="Home"/>
                            <MenuItem requriesRole="ROLE_SITE_MANAGER" targetPage={GM_SITES} label="Sites" />
                            <MenuItem requriesRole="ROLE_LOGS" targetPage={LOGS} label="Logs" />
                            <MenuItem requriesRole="ROLE_MISSION_MANAGER" targetPage={MISSIONS} label="Missions" />
                            <MenuItem requriesRole="ROLE_USER_MANAGER" targetPage={USERS} label="Users" />
                            <MenuItem requriesRole="ROLE_ADMIN" targetPage={ADMIN} label="Admin" />
                            <MenuItem requriesRole="ROLE_HACKER_MANAGER" targetPage={HACKER_COMMUNITY} label="Hacker Community" />
                        </ul>
                        <ul className="nav navbar-nav navbar-right">
                            <MenuItem requriesRole="ROLE_USER" targetPage={ME} label={ "{" + userName + "}"}/>
                            <li>
                                <a href="/manual" target="_blank">Manual</a>
                            </li>
                            <li>
                                <a href="/login" onClick={(event) => logout(event)}>ꕻ Logout</a>
                            </li>
                        </ul>
                    </div>
                </div>
            </div>
        )
    });
