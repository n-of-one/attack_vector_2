import React from 'react';
import {connect} from "react-redux";
import MenuBar from "../../common/menu/MenuBar";
import TextInput from "../../common/component/TextInput";
import {post} from "../../common/RestClient";
import {notify_fatal} from "../../common/Notification";
import {fetchSites} from "../FetchSites";

/* eslint jsx-a11y/accessible-emoji: 0 */
/* eslint jsx-a11y/anchor-is-valid: 0*/

const mapDispatchToProps = (dispatch) => {
    return {
        edit: (siteName) => {
            post({
                url: "/api/site/edit",
                body: {siteName: siteName},
                ok: ({id}) => {
                    window.open("/edit/" + id);
                    fetchSites(dispatch);
                },
                notok: () => {
                    notify_fatal("Connection to server failed, unable to continue.");
                }
            });

        }
    }
};
let mapStateToProps = (state) => {
    return {
        sites: state.sites
    };
};

export default connect(mapStateToProps, mapDispatchToProps)(
    ({sites, edit}) => {

        document.body.style.backgroundColor = "#222222";


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
                            Actions<br/>
                            <br/>
                            🌐 - print site scan<br/>
                            💠 - print SL version site scan<br/>
                            🞮 - deactivate site<br/>
                            💫 - reactivate site<br/>
                        </div>
                        <div id="actions">
                            <div className="text">
                                <TextInput placeholder="Site name"
                                           buttonLabel="Create or edit"
                                           buttonClass="btn-info"
                                           save={(siteName) => edit(siteName)}
                                           clearAfterSubmit="true"/>

                            </div>
                        </div>
                    </div>
                    <div className="col-lg-5 rightPane rightPane">
                        <div className="siteMap rightPaneDimensions">
                            <div>&nbsp;</div>
                            <div className="rightPanePadLeftRight">
                                <table className="table table-sm text-muted text" id="sitesTable">
                                    <thead>
                                    <tr>
                                        {/*<td className="text-strong">Link</td>*/}
                                        <td className="text-strong">Name</td>
                                        {/*<td className="text-strong">Hackable</td>*/}
                                        {/*<td className="text-strong">Action</td>*/}
                                    </tr>
                                    </thead>
                                    <tbody>
                                    {
                                        sites.map((site) => {
                                            return (
                                                <tr key="1">
                                                    <td className="table-very-condensed"><a target={site.id} href={"/edit/" + site.id }>{site.name}</a>
                                                    </td>
                                                    {/*<td className="table-very-condensed">{site.name}</td>*/}
                                                    {/*<td className="table-very-condensed">{site.hackable}</td>*/}
                                                    {/*<td className="table-very-condensed">*/}
                                                        {/*<a className="aimage" target="_blank" rel="noopener noreferrer"*/}
                                                           {/*href={"/gm/print/" + site.id + "/"} title="Print">🌐</a>*/}
                                                        {/*<a className="aimage" target="_blank" rel="noopener noreferrer"*/}
                                                           {/*href={"/gm/print-solution/" + site.id + "/"}*/}
                                                           {/*title="Print solution">💠</a>*/}
                                                        {/*<a className="aimage">🞮</a>*/}
                                                    {/*</td>*/}
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

            <MenuBar />

        </span>
    );
});
