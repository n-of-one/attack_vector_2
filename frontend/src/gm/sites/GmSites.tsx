import React, {useEffect} from 'react'
import {useDispatch, useSelector} from "react-redux"
import {TextInput} from "../../common/component/TextInput"
import {deleteCall, post} from "../../common/server/RestClient"
import {notify} from "../../common/util/Notification"
import {SilentLink} from "../../common/component/SilentLink"
import {GmSite, RECEIVE_SITES} from "./GmSitesReducer"
import {GmState} from "../GmRootReducer";


export const GmSites = () => {

    const sites = useSelector((state: GmState) => state.sites)
    const dispatch = useDispatch()


    const fetchSites = () => {
        fetch("/api/site/")
            .then(response => response.json())
            .then(sites => {
                dispatch({type: RECEIVE_SITES, sites: sites})
            })

    }

    useEffect(() => {
        fetchSites()
    }, [])

    const edit = (siteName: string) => {
        post({
            url: "/api/site/edit",
            body: {siteName: siteName},
            ok: ({id}: { id: string }) => {
                window.open("/edit/" + id)
            },
            notok: () => {
                notify({type: "fatal", message: "Connection to server failed, unable to continue."})
            },
            error: () => {
                notify({type: "fatal", message: "Connection to server failed, unable to continue."})
            }
        })

        setTimeout(fetchSites, 1000)
    }

    const deleteSite = (siteId: string, name: string) => {
        if (window.confirm(`Confirm that you want to delete site ${name}. `)) {
            deleteCall({
                url: `/api/site/${siteId}`,
                body: {},
                ok: () => {
                    window.location.reload()
                },
                notok: () => {
                    notify({type: "fatal", message: "Connection to server failed, unable to continue."})
                },
                error: () => {
                    notify({type: "fatal", message: "Connection to server failed, unable to continue."})
                }
            })
        }
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
                    Enter a site name and click one of the buttons.<br/>
                    The site does not have to exist yet.<br/>
                    <br/>
                </div>
                <div id="actions">
                    <div className="text">
                        <TextInput placeholder="Site name"
                                   buttonLabel="Create or edit"
                                   buttonClass="btn-info"
                                   save={(siteName: string) => edit(siteName)}
                                   clearAfterSubmit={true}/>

                    </div>
                </div>
            </div>
            <div className="col-lg-5 rightPane rightPane">
                <div className="siteMap">
                    <table className="table table-sm text-muted text" id="sitesTable">
                        <thead>
                        <tr>
                            {/*<td className="text-strong">Link</td>*/}
                            <td className="strong">Name</td>
                            {/*<td className="text-strong">Hackable</td>*/}
                            {/*<td className="text-strong">Action</td>*/}
                        </tr>
                        </thead>
                        <tbody>
                        {
                            sites.map((site: GmSite) => {
                                return (
                                    <tr key={site.id}>
                                        <td className="table-very-condensed"><SilentLink onClick={() => {
                                            window.open("/edit/" + site.id, site.id)
                                        }}><>{site.name}</>
                                        </SilentLink>
                                        </td>
                                        <td>
                                            <SilentLink onClick={() => {
                                                deleteSite(site.id, site.name);
                                            }}>
                                                <span className="glyphicon glyphicon-remove-circle"/>
                                            </SilentLink>
                                        </td>
                                    </tr>)
                            })
                        }
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    )
}
