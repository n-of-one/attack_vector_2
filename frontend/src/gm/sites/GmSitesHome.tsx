import React, {useEffect, useState} from 'react'
import {useSelector} from "react-redux"
import {TextInput} from "../../common/component/TextInput"
import {post} from "../../common/server/RestClient"
import {notify} from "../../common/util/Notification"
import {GmState} from "../GmRootReducer";
import {webSocketConnection} from "../../common/server/WebSocketConnection";
import {toServerUrl} from "../../common/util/DevEnvironment";
import {SiteList} from "./SitesTable";

/* eslint react-hooks/exhaustive-deps: 0*/

const askForSitesList = () => {
    webSocketConnection.sendWhenReady("/siteList", null)
}

export const GmSitesHome = () => {

    const sites = useSelector((state: GmState) => state.sites)
    const [file, setFile] = useState()

    useEffect(() => {
        askForSitesList()
    }, [])

    const edit = (siteName: string) => {
        post({
            url: "/api/site/edit",
            body: {siteName: siteName},
            ok: ({id}: { id: string }) => {
                window.open("/edit/" + id)
                askForSitesList()
            },
            notok: () => {
                notify({type: "fatal", message: "Connection to server failed, unable to continue."})
            },
            error: () => {
                notify({type: "fatal", message: "Connection to server failed, unable to continue."})
            }
        })
    }


    const uploadFile = (event: any) => {
        event.preventDefault()
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
                    Frontier Hacking GM Interface<br/>
                    <br/>
                    <br/>
                    Create a new site<br/>
                    <br/>
                </div>
                <div id="actions">
                    <div className="text">
                        <TextInput placeholder="Site name"
                                   buttonLabel="New"
                                   buttonClass="btn-info"
                                   save={(siteName: string) => edit(siteName)}
                                   clearAfterSubmit={true}/>

                    </div>
                </div>
                <br />
                <hr />
                <br />
                <div id="Upload">
                    <div className="text">
                        <label htmlFor="formFile" className="form-label">Import sites from file</label>
                        <form onSubmit={uploadFile}>
                            <div className="row">
                                <div className="col-lg-10">
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
            <div className="col-lg-5 rightPane rightPane">
                <SiteList sites={sites}/>
            </div>
        </div>
    )
}