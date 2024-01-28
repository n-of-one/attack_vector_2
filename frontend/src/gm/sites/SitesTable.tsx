import {GmSite} from "./GmSitesReducer";
import {SilentLink} from "../../common/component/SilentLink";
import React from "react";
import {restDelete} from "../../common/server/RestClient";
import {notify} from "../../common/util/Notification";
import {webSocketConnection} from "../../common/server/WebSocketConnection";
import {toServerUrl} from "../../common/util/DevEnvironment";


interface Props {
    sites: GmSite[]
}

export const SiteList = (props: Props) => {

    const sites = [...props.sites]
        .sort((a, b) => a.name.localeCompare(b.name))
        .sort((a, b)=> a.creator.localeCompare(b.creator))


    const deleteSite = (siteId: string, name: string) => {
        if (window.confirm(`Confirm that you want to delete site ${name}. `)) {
            restDelete({
                url: `/api/site/${siteId}`,
                body: {},
                ok: () => {
                    window.location.reload()
                },
                error: () => {
                    notify({type: "fatal", message: "Connection to server failed, unable to continue."})
                }
            })
        }
    }

    const resetSite = (siteId: string, name: string) => {
        if (window.confirm(`Confirm that you want to reset site ${name}. (Refresh ICE, reset all timers, change non-hardcoded passwords, ...)`)) {
            webSocketConnection.send("/site/resetSite", siteId)
        }
    }

    const deleteRuns = (siteId: string, name: string) => {
        if (window.confirm(`Confirm that you want to delete all runs for this site? This will also reset the site (refresh ICE, etc.)`)) {
            webSocketConnection.send("/site/deleteRuns", siteId)
        }
    }

    const download = (siteId: string) => {
        window.open(toServerUrl(`/api/export/site/${siteId}`))
    }

    const updateSiteHackable = (siteId: string, hackable: boolean) => {
        webSocketConnection.send("/site/updateHackable", {siteId: siteId, hackable: hackable})
    }


    return (
        <table className="table table-sm text-muted text" id="sitesTable">
            <thead>
            <tr>
                <td className="strong" style={{width: "300px"}}></td>
                <td className="strong" style={{width: "150px"}}></td>
                <td className="strong"></td>
            </tr>
            </thead>
            <tbody>
            {
                sites.map((site: GmSite) => {
                    return (
                        <tr key={site.id}>
                            <td className="table-very-condensed">
                                <input type="checkbox" checked={site.hackable} className="checkbox-inline" onChange={() => {updateSiteHackable(site.id, !site.hackable)}}/>&nbsp;
                                <SilentLink onClick={() => {
                                    window.open("/edit/" + site.id, site.id)
                                }}><>{site.name}</>
                                </SilentLink>
                            </td>
                            <td className="table-very-condensed">{site.creator}</td>
                            <td>
                                <SilentLink onClick={() => {
                                    resetSite(site.id, site.name);
                                }} title="Reset site">
                                    <span className="glyphicon glyphicon-refresh"/>
                                </SilentLink>
                                &nbsp;
                                <SilentLink onClick={() => {
                                    deleteRuns(site.id, site.name);
                                }} title="Delete all runs">
                                    <span className="glyphicon glyphicon-remove-circle"/>
                                </SilentLink>
                                &nbsp;
                                &nbsp;
                                <SilentLink onClick={() => {
                                    download(site.id);
                                }} title="Download">
                                    <span className="glyphicon glyphicon-download"/>
                                </SilentLink>
                                &nbsp;
                                &nbsp;
                                <SilentLink onClick={() => {
                                    deleteSite(site.id, site.name);
                                }} title="Delete site">
                                    <span className="glyphicon glyphicon-trash"/>
                                </SilentLink>
                            </td>
                        </tr>)
                })
            }
            </tbody>
        </table>
    )

}