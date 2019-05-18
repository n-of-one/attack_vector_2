import React from 'react';
import {connect} from "react-redux";
import {NavLink} from "react-router-dom";
import MenuBar from "../../common/menu/MenuBar";
import TextInput from "../../common/component/TextInput";
import {post} from "../../common/RestClient";
import {notify, notify_fatal} from "../../common/Notification";
import SilentLink from "../../common/component/SilentLink";

/* eslint jsx-a11y/accessible-emoji: 0 */
/* eslint jsx-a11y/anchor-is-valid: 0*/

let scan = (siteName) => {
    post({
        url: "/api/scan/site",
        body: {siteName: siteName},
        ok: ({scanId, message}) => {
            if (scanId) {
                document.location.href = "/hacker/scan/" + scanId;
            }
            else {
                notify(message);
            }
        },
        notok: () => {
            notify_fatal("Connection to server failed, unable to continue.");
        }
    });


};

const mapDispatchToProps = (dispatch) => {
    return {
        scanSite: scan
    };
};

let mapStateToProps = (state) => {
    return {
        scans: state.scans
    };
};

export default connect(mapStateToProps, mapDispatchToProps)(
    ({scanSite, scans}) => {

        return (
            <span>
            
            <div className="container">
                <div className="row">
                    <div className="col-lg-2">
                        <span className="text">&nbsp;</span>
                    </div>
                    <div className="col-lg-5 backgroundLight">
                        <span className="text">&nbsp;</span>
                    </div>
                    <div className="col-lg-5 rightPane">
                        <span className="text">Scans</span>
                    </div>
                </div>

                <div className="row">
                    <div className="col-lg-2">
                    </div>
                    <div className="col-lg-5">
                        <div className="text">
                            <strong>🜁 Verdant OS. 🜃</strong><br/>
                            <br/>
                            Choose site to investigate or attack<br/>
                            <br/>
                            <br/>
                        </div>
                        <div id="actions">
                            <div className="text">
                                <TextInput placeholder="Site name"
                                           buttonLabel="Scan"
                                           buttonClass="btn-info"
                                           save={(siteName) => scanSite(siteName)}
                                           clearAfterSubmit="true"/>

                            </div>
                            <div>
                                <NavLink to="/scan/site-123" target="_blank">Edit</NavLink>
                            </div>
                        </div>
                    </div>
                    <div className="col-lg-5 rightPane rightPane">
                        <div className="siteMap rightPaneDimensions">
                            <div>&nbsp;</div>
                            <div className="rightPanePadLeftRight">
                                <table className="table table-condensed text-muted text" id="sitesTable">
                                    <thead>
                                    <tr>
                                        <td className="strong">Site Name</td>
                                        <td className="strong">Complete</td>
                                        <td className="strong">Scan ID</td>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    {
                                        scans.map((scanLine) => {
                                            return (
                                                <tr key={scanLine.scanId}>
                                                    <td className="table-very-condensed">
                                                        <SilentLink href={"/hacker/scan/" + scanLine.scanId + "/"}>{scanLine.siteName}</SilentLink>
                                                    </td>
                                                    <td className="table-very-condensed">{(scanLine.complete) ? "yes": "no"}</td>
                                                    <td className="table-very-condensed">{scanLine.scanId}</td>
                                                </tr>);
                                        })
                                    }
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>
                </div>

            </div>
                {/* container*/}
                <MenuBar/>
        </span>

        );
    });
