import React from 'react'
import {useSelector} from "react-redux"
import Cookies from "js-cookie"
import {MenuItem} from "./MenuItem"
import {HackerRootState} from "../../hacker/HackerRootReducer"
import {ROLE_ADMIN, ROLE_GM, ROLE_HACKER, ROLE_SITE_MANAGER, ROLE_USER_MANAGER} from "../user/UserAuthorizations";
import {HackerSkillType, hasSkill} from "../users/CurrentUserReducer";
import {Page} from "./pageReducer";

/* eslint jsx-a11y/anchor-is-valid: 0*/

const logout = (event: any) => {
    event.preventDefault()
    Cookies.remove("jwt")
    Cookies.remove("type")
    Cookies.remove("roles")
    Cookies.remove("userName")
    document.location.href = "/loggedOut"
}

const scanItem = (currentPage: string, runName: string | null) => {
    if (currentPage === Page.RUN && runName) {
        return (
            <MenuItem requriesRole="ROLE_HACKER" targetPage={Page.RUN} label={"> " + runName}/>
        )
    } else {
        return <span/>
    }
}

export const MenuBar = () => {

    const userName = Cookies.get("userName")

    const siteName = useSelector((state: HackerRootState) => (state.run && state.run.site.siteProperties) ? state.run.site.siteProperties.name : "")
    const currentPage = useSelector((state: HackerRootState) => state.currentPage)

    const currentUser = useSelector((state: HackerRootState) => state.currentUser)


    const hackersCanCreateSites = hasSkill(currentUser, HackerSkillType.CREATE_SITE)
    const hackerHasScriptsSkill = hasSkill(currentUser, HackerSkillType.SCRIPT_RAM)

    const hackerSites = hackersCanCreateSites ? <MenuItem requriesRole={ROLE_HACKER} targetPage={Page.SITES} label="Sites"/> : <></>
    const hackerScripts = hackerHasScriptsSkill ? <MenuItem requriesRole={ROLE_HACKER} targetPage={Page.HACKER_SCRIPTS} label="Scripts"/> : <></>

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
                                <li className="nav-item"><a className="nav-link" href="/about" target="_blank">↼ Attack Vector ⇁</a></li>
                                {hackerSites}
                                {hackerScripts}
                                <MenuItem requriesRole="ROLE_HACKER" targetPage={Page.HACKER_HOME} label="Home"/>
                                {scanItem(currentPage, siteName)}
                                <MenuItem requriesRole={ROLE_GM} targetPage={Page.GM_SCRIPTS_HOME} label="Scripts"/>
                                <MenuItem requriesRole={ROLE_SITE_MANAGER} targetPage={Page.SITES} label="Sites"/>
                                <MenuItem requriesRole={ROLE_USER_MANAGER} targetPage={Page.USERS} label="Users"/>
                                <MenuItem requriesRole={ROLE_ADMIN} targetPage={Page.CONFIG} label="Config"/>
                                <MenuItem requriesRole={ROLE_ADMIN} targetPage={Page.TASKS} label="Tasks"/>
                            </ul>
                            <ul className="navbar-nav">
                                <MenuItem requriesRole="ROLE_USER" targetPage={Page.ME} label={"{" + userName + "}"}/>
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
