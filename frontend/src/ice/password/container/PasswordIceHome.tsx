import React from 'react'
import {useDispatch, useSelector} from "react-redux"
import {Terminal} from "../../../common/terminal/Terminal"
import {ICE_PASSWORD_LOCK, PasswordIceI} from "./PasswordIceReducer"
import {TERMINAL_SUBMIT, TerminalState} from "../../../common/terminal/TerminalReducer"
import {Dispatch} from "redux"
import {formatTimeInterval} from "../../../common/util/Util";
import {ENTER_KEY} from "../../../KeyCodes";
import {webSocketConnection} from "../../../common/server/WebSocketConnection";
import {PasswordRootState} from "../PasswordRootReducer";
import {HIDDEN, LOCKED, UNLOCKED} from "../../IceModel";

const renderInput = (inputTerminal: TerminalState, enterPassword: () => void, dispatch: Dispatch, ice: PasswordIceI) => {
    if (ice.uiState === LOCKED) {
        return <></>
    }
    if (ice.waitSeconds && ice.waitSeconds > 0) {

        const wait = formatTimeInterval(ice.waitSeconds)

        return <h4 className="text-warning">
            <strong>
                Time-out: <span className="text-info">{wait}</span><br/>
            </strong>
        </h4>
    }

    return <Terminal terminalState={inputTerminal} submit={enterPassword} />
}

const renderHint = (ice: PasswordIceI) => {
    if (ice.hint) {
        return <div className="text"><em>Password hint: {ice.hint}</em><br/></div>
    } else {
        return <></>
    }
}


export const PasswordIceHome = () => {

    const dispatch = useDispatch()

    const iceId = useSelector((state: PasswordRootState) => state.iceId)
    const ice = useSelector((state: PasswordRootState) => state.password)
    const displayTerminal = useSelector((state: PasswordRootState) => state.displayTerminal)
    const inputTerminal = useSelector((state: PasswordRootState) => state.inputTerminal)

    const enterPassword = () => {
        const password = inputTerminal.input
        dispatch({type: TERMINAL_SUBMIT, key: ENTER_KEY, command: password, terminalId: inputTerminal.id})
        if (ice.uiState !== UNLOCKED || ice.waitSeconds > 0 || password.trim().length === 0) {
            return
        }

        const payload = {iceId: iceId, password: password}
        webSocketConnection.sendObject("/av/ice/password/submit", payload)
        dispatch({type: ICE_PASSWORD_LOCK})
    }
    const classHidden = ice.uiState === HIDDEN ? " hidden_alpha" : ""


    return (
        <div className="row passwordIcePanelRow">
            <div className="col-lg-12">
                <div className="row">
                    <div className="col-lg-12">
                        <h4 className="text-success">
                            <strong>
                                Ice: <span className="text-info">Rahasy</span>&nbsp;<br/>
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
                            </div>
                        </div>
                    </div>
                    <div className="col-lg-9">
                        <Terminal terminalState={displayTerminal} height={112}/>
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
                                Passwords tried
                            </strong>
                        </h4>
                        <ul>
                            {ice.attempts.map((attempt, index) => <li key={index}>{attempt}</li>)}
                        </ul>
                    </div>
                </div>


            </div>
        </div>
    )
}
