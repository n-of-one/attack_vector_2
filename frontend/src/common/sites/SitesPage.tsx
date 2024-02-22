import React, {useState} from 'react'
import {useSelector} from "react-redux"
import {TextInput} from "../component/TextInput"
import {GmState} from "../../gm/GmRootReducer";
import {webSocketConnection} from "../server/WebSocketConnection";
import {toServerUrl} from "../util/DevEnvironment";
import {SiteList} from "./SitesTable";
import {SiteInfo} from "./SitesReducer";
import userAuthorizations, {ROLE_GM} from "../user/UserAuthorizations";

/* eslint react-hooks/exhaustive-deps: 0*/

const askForSitesList = () => {

}

export const SitesPage = () => {

    const sites = useSelector((state: GmState) => state.sites)


    const [file, setFile] = useState()

    const edit = (siteName: string) => {
        webSocketConnection.send("/editor/open", siteName)
    }

    const uploadFile = (event: any) => {
        event.preventDefault()
        if (!file) {
            return
        }
        const formData = new FormData()
        formData.append("file", file as unknown as string)
        fetch(toServerUrl("/api/import/site"), {
            method: "POST", body: formData, credentials: "include"
        }).then(() => {
            askForSitesList()
        })
    }

    const selectFile = (event: any) => {
        setFile(event.target.files[0])
    }

    return (
        <div className="row">
            <div className="col-lg-2">
            </div>
            <div className="col-lg-5">
                <div className="text">
                    <strong>🜁ttack 🜃ector</strong><br/>
                    <br/>
                    <br/>
                    <br/>
                    Create new site<br/>
                    <br/>
                </div>
                <div id="actions">
                    <div className="text">
                        <TextInput placeholder="Site name"
                                   buttonLabel="New"
                                   buttonClass="btn-info"
                                   save={(siteName: string) => edit(siteName)}
                                   clearAfterSubmit={true}
                                   size={8}
                        />

                    </div>
                </div>
                <br/>
                <hr/>
                <br/>
                <div className="text">Import site from file</div>
                <br/>
                <div id="Upload">
                    <div className="text">
                        <form onSubmit={uploadFile}>
                            <div className="row">
                                <div className="col-lg-8">
                                    <input className="form-control" type="file" id="formFile" onChange={selectFile}/>
                                </div>
                                <div className="col-lg-2">
                                    <button className="btn btn-info" type="submit" style={{fontSize: "12px"}}>Upload</button>
                                </div>
                            </div>
                        </form>

                    </div>
                </div>
            </div>
            <div className="col-lg-5 rightPane">
                <SitesPanel sites={sites}/>
            </div>
        </div>
    )
}

interface SitesPanelProperties {
    sites: SiteInfo[]
}

const SitesPanel = ({sites}: SitesPanelProperties) => {

    const isGm = userAuthorizations.roles.includes(ROLE_GM);

    const mySites = sites.filter(site => site.mine)
    const otherSites = sites.filter(site => !site.mine)


    const hackableSites = mySites.filter(site => site.hackable)
    const unhackableSites = mySites.filter(site => !site.hackable)

    return (
        <div className="siteMap">
            <div className="text">Hackable sites</div>
            <SiteList sites={hackableSites}/>
            <div className="text">Unhackable sites</div>
            <SiteList sites={unhackableSites}/>
            {isGm && <><br/><div className="text">Other user's sites</div></>}
            {isGm && <SiteList sites={otherSites}/>}
        </div>
    )
}