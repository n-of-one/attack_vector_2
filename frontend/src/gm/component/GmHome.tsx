import React from 'react'
import {useDispatch, useSelector} from "react-redux"
import MenuBar from "../../common/menu/MenuBar"
import {TextInput} from "../../common/component/TextInput"
import {post} from "../../common/RestClient"
import {notify_fatal} from "../../common/Notification"
import SilentLink from "../../common/component/SilentLink"
import {GmState} from "../GmRoot"
import {GmSite, RECEIVE_SITES} from "../GmSitesReducer"
import {useRunOnce} from "../../common/Util"



export const GmHome = () => {

    const sites = useSelector((state: GmState) => state.sites)
    const dispatch = useDispatch()


    useRunOnce(() => {
        fetch("/api/site/")
            .then(response => response.json())
            .then(sites => {
                dispatch({type: RECEIVE_SITES, sites: sites})
            })
    })

    const edit = (siteName: string) => {
        post({
            url: "/api/site/edit",
            body: {siteName: siteName},
            ok: ({id}: { id: string }) => {
                window.open("/edit/" + id)
            },
            notok: () => {
                notify_fatal("Connection to server failed, unable to continue.")
            },
            error: () => {
                notify_fatal("Connection to server failed, unable to continue.")
            }
        })
    }


    return (

        <div className="container-fluid" data-bs-theme="dark">
            <div className="row">
                <div className="col-lg-2">
                    <span className="text">&nbsp;</span>
                </div>
                <div className="col-lg-5 backgroundLight">
                    <span className="text">&nbsp;</span>
                </div>
                <div className="col-lg-5 rightPane">
                    <span className="text">Site map</span>
                </div>
            </div>


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
                                        </tr>)
                                })
                            }
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
            <MenuBar/>
        </div>


    )
}
