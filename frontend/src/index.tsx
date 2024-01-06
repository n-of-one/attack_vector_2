import React from 'react'
import {createRoot} from 'react-dom/client'
import {BrowserRouter, Route, Routes, useParams} from 'react-router-dom'
import {GmRoot} from "./gm/GmRoot"
import {EditorRoot} from "./editor/EditorRoot"
import {HackerRoot} from "./hacker/HackerRoot"
import Cookies from "js-cookie"
import {ToasterConfig} from "./common/util/Notification";
import {larp} from "./common/Larp";
import {Standalone} from "./standalone/Standalone";
import {LoggedOut} from "./login/LoggedOut";
import {About} from "./About";
import {Privacy} from "./Privacy";
import {AdminLogin} from "./login/AdminLogin";

console.log("\nWelcome to _Attack Vector_" +
    "\n" +
    "\nUsing the browser console / dev-tools is not part of this game." +
    "\nYou don't need to do any real-life hacking to play this game." +
    "\nReal-life hacking is against the spirit of this game. Please close the developer tools if you are seeing this in a LARP." +
    "\n" +
    "\n_Attack Vector_ is open source: https://github.com/n-of-one/attack_vector_2" +
    "\n ")


const ReRoute = (): React.JSX.Element => {

    let type = Cookies.get("type")
    if (type === "ADMIN") {
        window.document.location.href = "/gm/"
        return (<></>)
    }
    if (type === "GM") {
        window.document.location.href = "/gm/"
        return (<></>)
    }
    if (type === "HACKER" || type === "HACKER_MANAGER") {
        window.document.location.href = "/hacker/"
        return (<></>)
    }
    console.log("Unknown user type: " + type)
    Cookies.remove("jwt")
    Cookies.remove("type")
    Cookies.remove("roles")
    window.document.location.href = larp.loginUrl
    return (<></>)
}

const container = document.getElementById('app') as HTMLDivElement
const root = createRoot(container)

const Editor = () => {
    const {siteId} = useParams()
    return (<EditorRoot siteId={siteId as string}/>)
}


root.render(
    <>
        <BrowserRouter>
            <Routes>
                <Route path="/login" element={larp.loginElement()}/>
                <Route path="/adminLogin" element={<AdminLogin/>}/>
                <Route path="/loggedOut" element={<LoggedOut/>}/>
                <Route path="/about" element={<About/>}/>
                <Route path="/privacy" element={<Privacy/>}/>
                <Route path="/hacker" element={<HackerRoot/>}/>
                <Route path="/gm" element={<GmRoot/>}/>
                <Route path="/edit/:siteId" element={<Editor/>}/>
                <Route path="/x/:encodedParam" element={<Standalone/>}/>
                <Route path="/" element={<ReRoute/>}/>
                <Route path="*" element={<ReRoute/>}/>
            </Routes>
        </BrowserRouter>
        <ToasterConfig/>
    </>
)
