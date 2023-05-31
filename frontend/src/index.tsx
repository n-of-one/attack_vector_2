import React from 'react'
import {createRoot} from 'react-dom/client'
import {BrowserRouter, Routes, Route, useParams} from 'react-router-dom'
import {GmRoot} from "./gm/GmRoot"
import {EditorRoot} from "./editor/EditorRoot"
import {Login} from "./login/Login"
import {HackerRoot} from "./hacker/HackerRoot"
import Cookies from "js-cookie"
import {ToasterConfig} from "./common/util/Notification";
import {IceRoot} from "./ice/IceRoot";
import {BannerPage} from "./login/Sso";
import {RequiresRole} from "./common/user/RequiresRole";
import {larp} from "./common/Larp";
import {StatusLightRoot} from "./widget/status_light/StatusLightRoot";
import {app} from "./app/AppId";
import {SwitchRoot} from "./app/switch/SwitchRoot";
import {ROLE_USER} from "./common/user/UserAuthorizations";


const ReRoute = (): JSX.Element => {

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

const Ice = () => {
    const {iceId} = useParams()
    return <RequiresRole requires="ROLE_HACKER">
            <IceRoot redirectId={iceId as string}/>
        </RequiresRole>
}

const Widget = () => {
    const {id} = useParams()
    app.id = id
    return <StatusLightRoot/>
}

const App = () => {
    const {id} = useParams()
    app.id = id
    // return <RequiresRole requires="ROLE_USER">
        return <SwitchRoot/>
    // </RequiresRole>
}


root.render(
    <>
        <BrowserRouter>
            <Routes>
                <Route path="/ice/:iceId" element={<Ice/>}/>
                <Route path="/login" element={<Login/>}/>
                <Route path="/loggedOut" element={<BannerPage/>}/>
                <Route path="/hacker" element={<HackerRoot/>}/>
                <Route path="/gm" element={<GmRoot/>}/>
                <Route path="/edit/:siteId" element={<Editor/>}/>
                <Route path="/widget/:id" element={<Widget/>}/>
                <Route path="/app/:id" element={<App/>}/>
                <Route path="/" element={<ReRoute/>}/>
                <Route path="*" element={<ReRoute/>}/>
            </Routes>
        </BrowserRouter>
        <ToasterConfig/>
    </>
)


