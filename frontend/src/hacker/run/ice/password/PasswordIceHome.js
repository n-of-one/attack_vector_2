import React from 'react';
import {connect} from "react-redux";
import Terminal from "../../../../common/terminal/Terminal";
import {ICE_PASSWORD_SUBMIT} from "./PasswordIceActions";

const mapDispatchToProps = (dispatch) => {
    return {
        dispatch: dispatch,
        enterPassword: (password) => dispatch({type: ICE_PASSWORD_SUBMIT, password: password}),
    }
};
let mapStateToProps = (state) => {
    return {
        displayTerminal: state.run.ice.displayTerminal,
        inputTerminal: state.run.ice.inputTerminal,
        ice: state.run.ice.password
    };
};

const renderInput = (inputTerminal, enterPassword, dispatch, ice) => {
    if (ice.locked) {
        return <></>;
    }
    if (ice.waitSeconds && ice.waitSeconds > 0) {
        return <div className="text"><span className="text-info"><br/>Time-out in progress.</span></div>
    }

    return <Terminal terminal={inputTerminal} submit={enterPassword} dispatch={dispatch}/>;
};

const renderHint = (ice) => {
    if (ice.hint) {
        return <div className="text"><em>Password hint: {ice.hint}</em><br/></div>
    } else {
        return <></>
    }
};

export default connect(mapStateToProps, mapDispatchToProps)(
    ({displayTerminal, inputTerminal, enterPassword, dispatch, ice}) => {

        let waitSeconds = (ice.waitSeconds && ice.waitSeconds > 0) ? "" + ice.waitSeconds : "00";

        if (waitSeconds.length < 2) {
            waitSeconds = "0" + waitSeconds
        }

        return (
            <div className="row icePanelRow">
                <div className="col-lg-12">
                    <br/>
                    <div className="row">
                        <div className="col-lg-3">
                            <div className="text-left">
                                <h4 className="text-success">
                                    <strong>
                                        Ice: <span className="text-info">Aruna</span><br/>
                                        Strength: <span className="text-info">Unknown</span><br/>
                                    </strong>
                                </h4>
                                <div className="text">
                                    Community &nbsp;avg 14:33 &nbsp;best 03:33 &nbsp;(44%)<br/>
                                    You &nbsp; &nbsp; &nbsp; &nbsp;avg 12:00 &nbsp;best 08:23 &nbsp;(85%)<br/>
                                    You &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; -02:33 &nbsp; &nbsp; &nbsp;+04:50<br/>
                                </div>
                            </div>
                        </div>
                        <div className="col-lg-9">
                            <Terminal className="displayTerminal" terminal={displayTerminal} dispatch={dispatch} height={112}/>
                        </div>
                    </div>
                    <hr style={{borderTopColor: "#300", marginTop: "5px", marginBottom: "5px"}}/>

                    <div className="row">
                        <div className="col-lg-6">
                            <br/>
                            <h4 className="text-success">
                                <strong>
                                    Time-out: <span className="text-info">0:{waitSeconds}</span><br/>
                                </strong>
                            </h4>
                            {renderHint(ice)}
                            {renderInput(inputTerminal, enterPassword, dispatch, ice)}<br/>

                        </div>
                        <div className="col-lg-6 text">
                            Passwords tried:<br/>
                            <br/>
                            <ul>
                                {ice.status.attempts.map((attempt, index) => <li key={index}>{attempt}</li>)}
                            </ul>
                        </div>
                    </div>


                </div>
            </div>
        );
    });
