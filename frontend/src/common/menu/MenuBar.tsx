import React from 'react'
import {useSelector} from "react-redux"
import Cookies from "js-cookie"
import {MenuItem} from "./MenuItem"
import {CONFIG, HACKER_HOME, ME, RUN, SITES, TASKS, USERS} from "./pageReducer"
import {HackerRootState} from "../../hacker/HackerRootReducer"
import {ROLE_ADMIN, ROLE_HACKER, ROLE_HACKER_MANAGER, ROLE_SITE_MANAGER, ROLE_USER_MANAGER} from "../user/UserAuthorizations";
import {HackerSkillType} from "../users/UserReducer";
import {hasSkill} from "../../hacker/SkillsReducer";

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

    const siteName = useSelector((state: HackerRootState) => (state.run && state.run.site.siteProperties) ? state.run.site.siteProperties.name : "")
    const currentPage = useSelector((state: HackerRootState) => state.currentPage)

    const skills = useSelector((state: HackerRootState) => state.skills)

    const hackersCanCreateSites = (skills) ? hasSkill(skills, HackerSkillType.CREATE_SITE) : false

    const createSites = hackersCanCreateSites ? <MenuItem requriesRole={ROLE_HACKER} targetPage={SITES} label="Sites"/> : <></>

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
                                {createSites}
                                <MenuItem requriesRole="ROLE_HACKER" targetPage={HACKER_HOME} label="Home"/>
                                {scanItem(currentPage, siteName)}
                                <MenuItem requriesRole={ROLE_SITE_MANAGER} targetPage={SITES} label="Sites"/>
                                <MenuItem requriesRole={ROLE_USER_MANAGER} targetPage={USERS} label="Users"/>
                                <MenuItem requriesRole={ROLE_HACKER_MANAGER} targetPage={USERS} label="Users"/>
                                <MenuItem requriesRole={ROLE_ADMIN} targetPage={CONFIG} label="Config"/>
                                <MenuItem requriesRole={ROLE_ADMIN} targetPage={TASKS} label="Tasks"/>
                            </ul>
                            <ul className="navbar-nav">
                                <MenuItem requriesRole="ROLE_USER" targetPage={ME} label={"{" + userName + "}"}/>
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
