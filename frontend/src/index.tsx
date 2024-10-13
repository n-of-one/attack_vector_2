import React, {useEffect} from 'react'
import {createRoot} from 'react-dom/client'
import {BrowserRouter, Route, Routes, useParams} from 'react-router-dom'
import {EditorRoot} from "./editor/EditorRoot"
import Cookies from "js-cookie"
import {ToasterConfig} from "./common/util/Notification";
import {AdminLogin} from "./login/AdminLogin";
import {LoggedOut} from "./login/LoggedOut";
import {About} from "./About";
import {Privacy} from "./Privacy";
import {HackerRoot} from "./hacker/HackerRoot";
import {GmRoot} from "./gm/GmRoot";
import {Standalone} from "./standalone/Standalone";
import {WebsiteLandingPage} from "./website/WebsiteLandingPage";
import {WebsiteRouting} from "./website/WebsiteRouting";
import {Lola} from "./larp/frontier/Lola";
import {AdminRoot} from "./admin/AdminRoot";
import {GoogleAuth} from "./login/GoogleAuth";
import {DevLogin} from "./login/DevLogin";

console.log("\nWelcome to _Attack Vector_" +
    "\n" +
    "\nUsing the browser console / dev-tools is not part of this game." +
    "\nYou don't need to do any real-life hacking to play this game." +
    "\nReal-life hacking is against the spirit of this game. Please close the developer tools if you are seeing this at a LARP event." +
    "\n" +
    "\n_Attack Vector_ is open source: https://github.com/n-of-one/attack_vector_2" +
    "\n ")


const ReRoute = (): React.JSX.Element => {

    useEffect(() => {
        const type = Cookies.get("type")
        if (type === "ADMIN") {
            window.document.location.href = "/admin/"
        } else if (type === "GM") {
            window.document.location.href = "/gm/"
        } else if (type === "HACKER" || type === "HACKER_MANAGER") {
            window.document.location.href = "/hacker/"
        } else {
            Cookies.remove("jwt")
            Cookies.remove("type")
            Cookies.remove("roles")
            window.document.location.href = "/redirectToLogin"
        }
    }, []);
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
                {/* Website and public pages */}
                <Route path="/website" element={<WebsiteLandingPage/>}/>
                <Route path="/website/:path" element={<WebsiteRouting/>}/>
                <Route path="/about" element={<About/>}/>
                <Route path="/privacy" element={<Privacy/>}/>

                {/* In game pages */}
                <Route path="/hacker" element={<HackerRoot/>}/>
                <Route path="/gm" element={<GmRoot/>}/>
                <Route path="/admin" element={<AdminRoot/>}/>

                <Route path="/edit/:siteId" element={<Editor/>}/>

                <Route path="/x/:encodedParam" element={<Standalone/>}/>
                <Route path="/o/:encodedParam" element={<Standalone/>}/>

                {/* Login and logout */}
                <Route path="/login" element={<GoogleAuth/>}/>
                <Route path="/adminLogin" element={<AdminLogin/>}/>
                <Route path="/devLogin" element={<DevLogin/>}/>

                <Route path="/loggedOut" element={<LoggedOut/>}/>
                {/*This happens when running in development on port 3000, there is no backend that redirects to the correct login*/}
                <Route path="/redirectToLogin" element={<DevLogin/>}/>


                {/* Larp specific */}
                <Route path="/larp/frontier/lola" element={<Lola/>}/>

                {/* Not specified, reroute to meaningful path */}
                <Route path="/" element={<ReRoute/>}/>
                <Route path="*" element={<ReRoute/>}/>
            </Routes>
        </BrowserRouter>
        <ToasterConfig/>
    </>
)
