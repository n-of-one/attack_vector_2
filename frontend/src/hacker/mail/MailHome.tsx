import React from 'react';
import {useDispatch, useSelector} from "react-redux";
import TimeStamp from "../../common/menu/TimeStamp";
import SilentLink from "../../common/component/SilentLink";
import {HackerState} from "../HackerRootReducer";
import {MailLine, MailState} from "./MailsReducer";
import {SELECT_MAIL} from "./CurrentMailReducer";


const renderLines = (lines: MailLine[]) => {
    let count = 0;
    return lines.map(line => {
        const key = "mailLine" + count
        count++
        return <div key={key}>{line.data}</div>
    });
};

const renderMail = (mails: MailState, currentMail: string | null) => {
    if (currentMail === null) {
        return <div> No mails selected</div>
    }
    const mail = mails[currentMail];
    if (mail === null) {
        return <div>Mail not found</div>
    }

    return <div className="text">
        Date: <TimeStamp timestamp={mail.timestamp}/><br/>
        From: <span className="text-primary strong">{mail.from}</span><br/>
        <hr/>
        {renderLines(mail.lines)}
        <br/>
    </div>


};

// export default connect(mapStateToProps, mapDispatchToProps)(
//     ({mails, selectMail, currentMail}) => {

export const MailHome = () => {

    const dispatch = useDispatch()
    const selectMail = (id: string) => {
        dispatch({type: SELECT_MAIL, mailId: id})
    }

    const mails: MailState = useSelector((state: HackerState) => state.mail.mails)
    const mailList = Object.keys(mails).map((key, index) => mails[key]);
    const currentMail: string | null = useSelector((state: HackerState) => state.mail.currentMail)

    return (
        <div className="row">
            <div className="col-lg-6">
                <div className="row">
                    <div className="col-lg-12 backgroundLight">
                        <span className="text">&nbsp;</span>
                    </div>
                </div>
                <div className="row">
                    <div className="col-lg-12">
                        <div className="text">
                            <br/>
                            <div>Mail</div>
                            <br/>
                            {renderMail(mails, currentMail)}
                        </div>
                    </div>
                </div>
            </div>
            <div className="col-lg-6 rightPane rightPane">
                <div className="row">
                    <div className="col-lg-12">
                        <span className="text">Mails</span>
                    </div>
                </div>
                <div className="row">
                    <div className="col-lg-12">

                        <div className="siteMap rightPaneDimensions">
                            <div className="rightPanePadLeftRight">
                                <table className="table table-sm text-muted text" id="mails">
                                    <thead>
                                    <tr>
                                        <td className="strong">From</td>
                                        <td className="strong">Title</td>
                                        <td className="strong">Date</td>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    {
                                        mailList.map((mail) => {
                                            return (
                                                <tr key={mail.id}>
                                                    <td className="table-very-condensed" style={{width: "85px"}}>{mail.from}</td>
                                                    <td className="table-very-condensed" style={{width: "400px"}}>
                                                        <SilentLink onClick={() => {
                                                            selectMail(mail.id)
                                                        }}><>{mail.title}</>
                                                        </SilentLink>
                                                    </td>
                                                    <td className="table-very-condensed" style={{width: "122px"}}>
                                                        <TimeStamp timestamp={mail.timestamp}/>
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
        </div>

    );
}
