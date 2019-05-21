import React from 'react';
import {connect} from "react-redux";
import {NavLink} from "react-router-dom";
import TextInput from "../../common/component/TextInput";
import SilentLink from "../../common/component/SilentLink";
import {ENTER_SCAN, SCAN_FOR_NAME} from "./HomeActions";

/* eslint jsx-a11y/accessible-emoji: 0 */
/* eslint jsx-a11y/anchor-is-valid: 0*/


const mapDispatchToProps = (dispatch) => {
    return {
        scanSite: (siteName) => {
            dispatch({type: SCAN_FOR_NAME, siteName: siteName});
        },
        enterScan: (scanInfo) => {
            dispatch({type: ENTER_SCAN, data: {scanId: scanInfo.scanId, siteId: scanInfo.siteId}});
        }
    };
};

let mapStateToProps = (state) => {
    return {
        scans: state.home.scans
    };
};

export default connect(mapStateToProps, mapDispatchToProps)(
    ({scanSite, scans, enterScan}) => {

        return (
            <div className="row">
                <div className="col-lg-6">
                    <div className="row backgroundLight">
                        &nbsp;
                    </div>
                    <div className="row">
                        <div className="text">
                            <strong>🜁 Verdant OS 🜃</strong><br/>
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
                </div>
                <div className="col-lg-6 rightPane">

                    <div className="row">
                        <div className="col-lg-12">
                            <span className="text">Scans</span>
                        </div>
                    </div>
                    <div className="row">
                        <div className="col-lg-12">
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
                                            scans.map((scanInfo) => {
                                                return (
                                                    <tr key={scanInfo.scanId}>
                                                        <td className="table-very-condensed">
                                                            <SilentLink onClick={() => {
                                                                enterScan(scanInfo)
                                                            }}>{scanInfo.siteName}</SilentLink>
                                                        </td>
                                                        <td className="table-very-condensed">{(scanInfo.complete) ? "yes" : "no"}</td>
                                                        <td className="table-very-condensed">{scanInfo.scanId}</td>
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
            </div>


        );
    });

{/*<div className="row">*/}
{/*<div className="col-lg-2">*/}
{/*<span className="text">&nbsp;</span>*/}
{/*</div>*/}
{/*<div className="col-lg-5 backgroundLight">*/}
{/*<span className="text">&nbsp;</span>*/}
{/*</div>*/}
{/*<div className="col-lg-5 rightPane">*/}
{/*<span className="text">Scans</span>*/}
{/*</div>*/}
{/*</div>*/}