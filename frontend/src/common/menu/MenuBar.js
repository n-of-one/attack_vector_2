import React from 'react';
import {connect} from "react-redux";
import Cookies from "js-cookie";
import MenuItem from "./MenuItem";
import {ADMIN, GM_SITES, LOGS, MISSIONS, USERS} from "../../gm/GmPages";
import {HACKER_COMMUNITY, HACKER_HOME, SCAN,} from "../../hacker/HackerPages";
import {ME} from "./CommonPages";

/* eslint jsx-a11y/anchor-is-valid: 0*/

const mapDispatchToProps = (dispatch) => {
    return {}
};
let mapStateToProps = (state) => {

    const userName = Cookies.get("userName");
    const siteName = (state.run && state.run.site.siteData) ? state.run.site.siteData.name : "";

    return {
        userName: userName,
        currentPage: state.currentPage,
        runName: siteName,
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

const scanItem = (currentPage, runName) => {
    if (currentPage === SCAN && runName) {
        return (
            <MenuItem requriesRole="ROLE_HACKER" targetPage={SCAN} label={"> " + runName}/>
        )
    } else {
        return <span/>
    }
};

export default connect(mapStateToProps, mapDispatchToProps)(
    ({userName, currentPage, runName}) => {

        return (
            <nav className="navbar navbar-expand-sm navbar-av fixed-bottom" style={{
                borderStyle: "solid",
                borderWidth: "1px",
                borderColor: "black",
                padding: "0 0 0 0"
            }}>
                <div className="container">
                    {/*<div className="navbar-header">*/}
                    {/*    <button type="button" className="navbar-toggle" data-toggle="collapse"*/}
                    {/*            data-target=".nav-collapse">*/}
                    {/*        <span className="icon-bar"/>*/}
                    {/*        <span className="icon-bar"/>*/}
                    {/*        <span className="icon-bar"/>*/}
                    {/*    </button>*/}
                    {/*    <a className="navbar-brand" href="#">↼ Attack Vector ⇁</a>*/}
                    {/*</div>*/}
                    <div className="navbar-collapse">
                        <div className="container-fluid">
                            <div className="d-flex justify-content-between">
                                <ul className="navbar-nav mr-auto">
                                    <li className="nav-item"><a className="nav-link disabled" href="#">↼ Attack Vector
                                        ⇁</a>
                                    </li>
                                    {/*<MenuItem requriesRole="ROLE_HACKER" targetPage={SCRIPTS} label="Scripts" />*/}
                                    <MenuItem requriesRole="ROLE_HACKER" targetPage={HACKER_HOME} label="Home"/>
                                    {scanItem(currentPage, runName)}
                                    <MenuItem requriesRole="ROLE_SITE_MANAGER" targetPage={GM_SITES} label="Sites"/>
                                    <MenuItem requriesRole="ROLE_HACKER" targetPage={LOGS} label="Logs"/>
                                    <MenuItem requriesRole="ROLE_MISSION_MANAGER" targetPage={MISSIONS}
                                              label="Missions"/>
                                    <MenuItem requriesRole="ROLE_USER_MANAGER" targetPage={USERS} label="Users"/>
                                    <MenuItem requriesRole="ROLE_ADMIN" targetPage={ADMIN} label="Admin"/>
                                    <MenuItem requriesRole="ROLE_HACKER_MANAGER" targetPage={HACKER_COMMUNITY}
                                              label="Hacker Community"/>
                                </ul>
                                <ul className="navbar-nav">
                                    {/*<MenuItem requriesRole="ROLE_USER" targetPage={MAIL} label="Mail"/>*/}
                                    <MenuItem requriesRole="ROLE_USER" targetPage={ME} label={"{" + userName + "}"}/>
                                    {/*<li>*/}
                                    {/*<a href="/manual" target="_blank">Manual</a>*/}
                                    {/*</li>*/}
                                    <li className="nav-item">
                                        <a className="nav-link" href="/login" onClick={(event) => logout(event)}>ꕻ
                                            Logout</a>
                                    </li>
                                </ul>
                            </div>
                        </div>
                    </div>
                </div>
            </nav>
        )
    });
