import React from 'react';
import {connect} from "react-redux";
import {NavLink} from "react-router-dom";

/* eslint jsx-a11y/accessible-emoji: 0 */

const mapDispatchToProps = (dispatch) => {
    return {
    }
};
let mapStateToProps = (state) => {
    return {
    };
};

export default connect(mapStateToProps, mapDispatchToProps)(
    () => {

        document.body.style.backgroundColor = "#222222";


    let sites = [
        {id: "tutorial-site", hackable: false}
    ];

    let myUser = {loginName: "Admin"};

    return (
        <span>
            
            <div className="container">
                <div className="row">
                    <div className="col-lg-2">
                        <span className="text">&nbsp;</span>
                    </div>
                    <div className="col-lg-5 backgroundLight">
                        <span className="text">Cluster 16|20 &nbsp; Mem 1|10 &nbsp;</span>
                    </div>
                    <div className="col-lg-5 backgroundDark">
                        <span className="text">Site map</span>
                    </div>
                </div>



                <div className="row">
                    <div className="col-lg-2">
                    </div>
                    <div className="col-lg-5">
                        <div className="term terminal">
                            <strong>🜁ttack 🜃ector</strong><br/>
                            <br/>
                            Frontier Hacking SL Interface<br/>
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
                            <p className="text">
                                <div className="form-inline">
                                    <div className="form-group">
                                        <input type="text" className="form-control" id="siteName"
                                               placeholder="Site link"/>
                                    </div>
                                    <button type="button" className="btn btn-info" id="btn_edit">Create or edit</button>
                                </div>
                            </p>
                            <p>
                                <NavLink to="edit/tutorial.nfo:pra" target="_blank" >Edit</NavLink>
                            </p>
                        </div>
                    </div>
                    <div className="col-lg-5 backgroundDark rightPane">
                        <div className="dark_well rightPaneDimensions">
                            <div>&nbsp;</div>
                            <div className="rightPanePadLeftRight">
                                <table className="table table-condensed text-muted text" id="sitesTable">
                                    <thead>
                                    <tr>
                                        <td className="text-strong">Link</td>
                                        {/*<td className="text-strong">Name</td>*/}
                                        <td className="text-strong">Hackable</td>
                                        <td className="text-strong">Action</td>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    {
                                        sites.map((site) => {
                                            return (
                                                <tr>
                                                    <td className="table-very-condensed"><a target="_blank"
                                                                                            href={"/gm/editor/" + site.id + "/"}>{site.id}</a>
                                                    </td>
                                                    <td className="table-very-condensed">{site.hackable}</td>
                                                    <td className="table-very-condensed">
                                                        <a className="aimage" target="_blank"
                                                           href={"/gm/print/" + site.id + "/"} title="Print">🌐</a>
                                                        <a className="aimage" target="_blank"
                                                           href={"/gm/print-solution/" + site.id + "/"}
                                                           title="Print solution">💠</a>
                                                        <a className="aimage">🞮</a>
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
            
            <div className="navbar navbar-inverse navbar-fixed-bottom">
                <div className="container">
                    <div className="navbar-header">
                        <button type="button" className="navbar-toggle" data-toggle="collapse"
                                data-target=".nav-collapse">
                            <span className="icon-bar"/>
                            <span className="icon-bar"/>
                            <span className="icon-bar"/>
                        </button>
                        <a className="navbar-brand">↼ Attack Vector ⇁</a>
                    </div>
                    <div className="navbar-collapse collapse">
                        <ul className="nav navbar-nav">
                            <li className="active"><a>Sites</a></li>
                            <li><a href="/gm/user/">Users</a></li>
                            <li><a href="/gm/logs/">Logs</a></li>
                        </ul>
                        <ul className="nav navbar-nav navbar-right">
                            <li>
                                <a href="/manual" target="_blank">Manual</a>
                            </li>
                            <li>
                                <a href="/me/">ꕺ {myUser.loginName} </a>
                            </li>
                            <li>
                                <a href="/logout">ꕻ Logout</a>
                            </li>
                        </ul>
                    </div>
                </div>
            </div>
        </span>
    );
});
