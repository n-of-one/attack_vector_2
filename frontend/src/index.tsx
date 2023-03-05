import React from 'react'
import {createRoot} from 'react-dom/client'
import {BrowserRouter, Routes, Route, useParams} from 'react-router-dom'
import {GmRoot} from "./gm/GmRoot"
import {EditorRoot} from "./editor/EditorRoot"
import {Login} from "./Login"
import {HackerRoot} from "./hacker/HackerRoot"
import Cookies from "js-cookie"
import {ToasterConfig} from "./common/Notification";
import {IceRoot} from "./ice/IceRoot";


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
    window.document.location.href = "/login"
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
    return (<IceRoot redirectId={iceId as string}/>)
}

root.render(
    <>
        <BrowserRouter>
            <Routes>
                <Route path="/ice/:iceId" element={<Ice/>}/>
                <Route path="/login" element={<Login/>}/>
                <Route path="/hacker" element={<HackerRoot/>}/>
                <Route path="/gm" element={<GmRoot/>}/>
                <Route path="/edit/:siteId" element={<Editor/>}/>
                <Route path="/" element={<ReRoute/>}/>
                <Route path="*" element={<ReRoute/>}/>
            </Routes>
        </BrowserRouter>
        <ToasterConfig/>
    </>
)

