import React from 'react';
import {connect} from "react-redux";
import MenuBar from "../../../common/menu/MenuBar";
import TimeStamp from "../../../common/menu/TimeStamp";
import {SELECT_MAIL} from "../MailActions";
import SilentLink from "../../../common/component/SilentLink";

const mapDispatchToProps = (dispatch) => {
    return {
        selectMail: (id) => {dispatch({type: SELECT_MAIL, mailId: id}) },
    };
};

let mapStateToProps = (state) => {
    return {
        mails: state.mails,
        currentMail: state.currentMail,
    };
};

export default connect(mapStateToProps, mapDispatchToProps)(
    ({mails, selectMail, currentMail}) => {

        return (
            <span>

            <div className="container">
                <div className="row">
                    <div className="col-lg-2">
                        <div className="row">
                            <div className="col-lg-12">
                                <span className="text">&nbsp;</span>
                            </div>
                        </div>
                    </div>
                    <div className="col-lg-5">
                        <div className="row">
                            <div className="col-lg-12 backgroundLight">
                                <span className="text">&nbsp;</span>
                            </div>
                        </div>
                        <div className="row">
                            <div className="text">
                                <strong>🜁 Verdant OS. 🜃</strong><br/>
                                <br/>
                                Select mail: {currentMail}
                                <br/>
                                <br/>
                            </div>
                        </div>
                    </div>
                    <div className="col-lg-5 rightPane rightPane">
                        <div className="row">
                            <div className="col-lg-12">
                                <span className="text">&nbsp;</span>
                            </div>
                        </div>
                        <div className="row">
                            <div className="siteMap rightPaneDimensions">
                                <div>&nbsp;</div>
                                <div className="rightPanePadLeftRight">
                                    <table className="table table-condensed text-muted text" id="mails">
                                        <thead>
                                        <tr>
                                            <td className="strong" >From</td>
                                            <td className="strong">Title</td>
                                            <td className="strong">Date</td>
                                        </tr>
                                        </thead>
                                        <tbody>
                                        {
                                            mails.map((mail) => {
                                                return (
                                                    <tr key={mail.id}>
                                                        <td className="table-very-condensed" style={{width: "85px"}}>{mail.from}</td>
                                                        <td className="table-very-condensed" style={{width: "400px"}}>
                                                            <SilentLink onClick={() => {selectMail(mail.id)}}> {mail.title}</SilentLink>
                                                        </td>
                                                        <td className="table-very-condensed" style={{width: "122px"}}><TimeStamp timestamp={mail.timestamp} /></td>
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
                {/* container*/}
                <MenuBar/>
        </span>

        );
    });
