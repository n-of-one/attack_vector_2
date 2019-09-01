import React from 'react';
import {connect} from "react-redux";
import Terminal from "../../../../common/terminal/Terminal";
import {ICE_PASSWORD_SUBMIT} from "./PasswordIceActions";
import {HIDDEN, LOCKED} from "../IceUiState";
import CloseButton from "../../../../common/component/CloseButton";
import {FINISH_HACKING_ICE} from "../../model/HackActions";

const mapDispatchToProps = (dispatch) => {
    return {
        dispatch: dispatch,
        enterPassword: (password) => dispatch({type: ICE_PASSWORD_SUBMIT, password: password}),
        close: () => dispatch({type: FINISH_HACKING_ICE}),
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
    if (ice.uiState === LOCKED) {
        return <></>;
    }
    if (ice.waitSeconds && ice.waitSeconds > 0) {


        let waitSeconds = (ice.waitSeconds && ice.waitSeconds > 0) ? "" + ice.waitSeconds : "00";
        if (waitSeconds.length < 2) {
            waitSeconds = "0" + waitSeconds
        }


        return<h4 className="text-warning">
            <strong>
                Time-out: <span className="text-info">0:{waitSeconds}</span><br/>
            </strong>
        </h4>


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
    ({displayTerminal, inputTerminal, enterPassword, dispatch, ice, close}) => {

        const classHidden = ice.uiState === HIDDEN ? " hidden_alpha" : "";


        return (
            <div className="row passwordIcePanelRow">
                <div className="col-lg-12">
                    <div className="row">
                        <div className="col-lg-12">
                            <CloseButton closeAction={close} />
                            <h4 className="text-success">
                                <strong>
                                    Ice: <span className="text-info">Aruna</span><br/>
                                    Strength: <span className="text-info">Unknown</span><br/>
                                </strong>
                            </h4>
                        </div>
                    </div>
                    <hr style={{borderTopColor: "#300", marginTop: "5px", marginBottom: "5px"}}/>
                    <div className="row">
                        <div className="col-lg-3">
                            <div className="text-left">
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

                    <div className={"row transition_alpha_fast" + classHidden}>
                        <div className="col-lg-6">
                            <h4 className="text-success">
                                <strong>
                                    Password
                                </strong>
                            </h4>
                            {renderHint(ice)}
                            {renderInput(inputTerminal, enterPassword, dispatch, ice)}<br/>

                        </div>
                        <div className="col-lg-6 text">
                            <h4 className="text-success">
                                <strong>
                                    Already tried
                                </strong>
                            </h4>
                            <ul>
                                {ice.attempts.map((attempt, index) => <li key={index}>{attempt}</li>)}
                            </ul>
                        </div>
                    </div>


                </div>
            </div>
        );
    });
