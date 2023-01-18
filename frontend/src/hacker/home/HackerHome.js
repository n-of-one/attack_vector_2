import React from 'react';
import {connect} from "react-redux";
import {TextInput} from "../../common/component/TextInput";
import SilentLink from "../../common/component/SilentLink";
import {DELETE_SCAN, ENTER_SCAN, SCAN_FOR_NAME} from "./HomeActions";

/* eslint jsx-a11y/accessible-emoji: 0 */
/* eslint jsx-a11y/anchor-is-valid: 0*/


const mapDispatchToProps = (dispatch) => {
    return {
        scanSite: (siteName) => {
            if (siteName) {
                dispatch({type: SCAN_FOR_NAME, siteName: siteName});
            }
        },
        enterScan: (scanInfo) => {
            dispatch({type: ENTER_SCAN, data: {runId: scanInfo.runId, siteId: scanInfo.siteId}});
        },
        deleteScan: (scanInfo) => {
            dispatch({type: DELETE_SCAN, runId: scanInfo.runId});
        },
    };
};

let mapStateToProps = (state) => {
    return {
        scans: state.home.scans
    };
};

export default connect(mapStateToProps, mapDispatchToProps)(
    ({scanSite, scans, enterScan, deleteScan}) => {

        return (
            <div className="row">
                <div className="col-lg-6">
                    <div className="row">
                        <div className="col-lg-12">
                            <span className="text"><strong>🜁 Verdant OS 🜃</strong></span>
                        </div>
                    </div>
                    <div className="row">
                        <div className="col-lg-12">
                            <div className="text">
                                <br/>
                                Choose site to investigate or attack<br/>
                                <br/>
                                <br/>
                            </div>
                        </div>
                    </div>
                    <TextInput placeholder="Site name"
                               buttonLabel="Scan"
                               buttonClass="btn-info"
                               save={(siteName) => scanSite(siteName)}
                               clearAfterSubmit="true"/>
                </div>
                <div className="col-lg-6">
                    <div className="row">
                        <div className="col-lg-12">
                            <span className="text">Scans</span>
                        </div>
                    </div>
                    <div className="row">
                        <div className="col-lg-12">
                            <div className="siteMap">
                                <table className="table table-borderless table-sm text-muted text" id="sitesTable">
                                    <thead>
                                    <tr>
                                        <td className="strong">Site Name</td>
                                        <td className="strong">Nodes</td>
                                        <td className="strong">Initiator</td>
                                        <td className="strong">Efficiency</td>
                                        <td className="strong">&nbsp;</td>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    {
                                        scans.map((scanInfo) => {
                                            return (
                                                <tr key={scanInfo.runId}>
                                                    <td className="table-very-condensed">
                                                        <SilentLink title={scanInfo.runId} onClick={() => {
                                                            enterScan(scanInfo);
                                                        }}>{scanInfo.siteName}</SilentLink>
                                                    </td>
                                                    <td className="table-very-condensed">{scanInfo.nodes}</td>
                                                    <td className="table-very-condensed">{scanInfo.initiatorName}</td>
                                                    <td className="table-very-condensed">{scanInfo.efficiency}</td>
                                                    <td className="table-very-condensed">
                                                        <SilentLink onClick={() => {
                                                            deleteScan(scanInfo);
                                                        }}>
                                                            <span className="glyphicon glyphicon-remove-circle"/>
                                                        </SilentLink>
                                                    </td>
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


        );
    });
