import React from 'react'
import {useSelector} from "react-redux"
import Cookies from "js-cookie"
import {MenuItem} from "./MenuItem"
import {ADMIN, GM_SITES, LOGS, MISSIONS, USERS} from "../../gm/GmPages"
import {HACKER_COMMUNITY, HACKER_HOME, MAIL, ME, RUN} from "./pageReducer"
import {HackerState} from "../../hacker/HackerRootReducer"

/* eslint jsx-a11y/anchor-is-valid: 0*/

const logout = (event: any) => {
    event.preventDefault()
    Cookies.remove("jwt")
    Cookies.remove("type")
    Cookies.remove("roles")
    Cookies.remove("userName")
    document.location.href = "/login"
}

const scanItem = (currentPage: string, runName: string | null) => {
    if (currentPage === RUN && runName) {
        return (
            <MenuItem requriesRole="ROLE_HACKER" targetPage={RUN} label={"> " + runName}/>
        )
    } else {
        return <span/>
    }
}

export const MenuBar = () => {

    const userName = Cookies.get("userName")
    const siteName = useSelector( (state: HackerState ) =>  (state.run && state.run.site.siteProperties) ? state.run.site.siteProperties.name : "" )
    const currentPage =  useSelector( (state: HackerState) => state.currentPage )

    return (
        <nav className="navbar navbar-expand-sm navbar-av fixed-bottom" style={{
            borderStyle: "solid",
            borderWidth: "1px",
            borderColor: "black",
            padding: "0 0 0 0"
        }}>
            <div className="container">
                <div className="navbar-collapse">
                    <div className="container-fluid">
                        <div className="d-flex justify-content-between">
                            <ul className="navbar-nav mr-auto">
                                <li className="nav-item"><a className="nav-link disabled" href="#">↼ Attack Vector ⇁</a></li>
                                {/*<MenuItem requriesRole="ROLE_HACKER" targetPage={SCRIPTS} label="Scripts" />*/}
                                <MenuItem requriesRole="ROLE_HACKER" targetPage={HACKER_HOME} label="Home"/>
                                {scanItem(currentPage, siteName)}
                                <MenuItem requriesRole="ROLE_SITE_MANAGER" targetPage={GM_SITES} label="Sites"/>
                                <MenuItem requriesRole="ROLE_HACKER" targetPage={LOGS} label="Logs"/>
                                <MenuItem requriesRole="ROLE_HACKER" targetPage={MAIL} label="Mail"/>
                                <MenuItem requriesRole="ROLE_MISSION_MANAGER" targetPage={MISSIONS} label="Missions"/>
                                <MenuItem requriesRole="ROLE_USER_MANAGER" targetPage={USERS} label="Users"/>
                                <MenuItem requriesRole="ROLE_ADMIN" targetPage={ADMIN} label="Admin"/>
                                <MenuItem requriesRole="ROLE_HACKER_MANAGER" targetPage={HACKER_COMMUNITY}
                                          label="Hacker Community"/>
                            </ul>
                            <ul className="navbar-nav">
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
}
