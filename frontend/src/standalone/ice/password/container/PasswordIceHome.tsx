import React from 'react'
import {useDispatch, useSelector} from "react-redux"
import {Terminal} from "../../../../common/terminal/Terminal"
import {TERMINAL_CLEAR} from "../../../../common/terminal/TerminalReducer"
import {formatTimeInterval} from "../../../../common/util/Util";
import {webSocketConnection} from "../../../../common/server/WebSocketConnection";
import {PasswordRootState} from "../reducer/PasswordRootReducer";
import {HIDDEN} from "../../common/IceModel";
import {SUBMIT_PASSWORD, UI_STATE_LOCKED, UI_STATE_SUBMITTING, UI_STATE_UNLOCKED} from "../../../app/auth/reducer/AuthUiReducer";
import {ice} from "../../../StandaloneGlobals";
import {IceTitle} from "../../common/IceTitle";
import {IceStrength} from "../../../../common/model/IceStrength";
import {CloseTabButton} from "../../common/CloseTabButton";


export const PasswordIceHome = () => {
    const displayTerminal = useSelector((state: PasswordRootState) => state.displayTerminal)
    const hackUiState = useSelector((state: PasswordRootState) => state.hackUi.state)

    const classHidden = hackUiState === HIDDEN ? " hidden_alpha" : ""

    return (
        <div className="row passwordIcePanelRow">
            <div className="col-lg-12">
                <div className="row">
                    <div className="col-lg-11">
                        <IceTitle name="Rahasy" strength={IceStrength.UNKNOWN}/>
                    </div>
                    <div className="col-lg-1">
                        <div className="float-end">
                            <CloseTabButton/>
                        </div>
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
                        <PasswordHint/>
                        <PasswordInput />
                        <br/>
                    </div>
                    <div className="col-lg-6 text">
                        <h4 className="text-success">
                            <strong>
                                Passwords tried
                            </strong>
                            <PasswordAttempts />
                        </h4>
                    </div>
                </div>
            </div>
        </div>
    )
}


const PasswordHint = () => {
    const hint = useSelector((state: PasswordRootState) => state.iceInfo.hint)
    const showHint = useSelector((state: PasswordRootState) => state.ui.showHint)
    if (showHint) {
        return <div className="text"><em>Password hint: {hint}</em><br/></div>
    } else {
        return <></>
    }
}


const PasswordInput = () => {

    const dispatch = useDispatch()
    const inputTerminal = useSelector((state: PasswordRootState) => state.inputTerminal)
    const ui = useSelector((state: PasswordRootState) => state.ui)

    const enterPassword = () => {
        const password = inputTerminal.input
        if (ui.state !== UI_STATE_UNLOCKED || password.trim().length === 0) {
            return
        }
        dispatch({type: TERMINAL_CLEAR, terminalId: inputTerminal.id})

        const payload = {iceId: ice.id, password: password }
        webSocketConnection.sendObject("/ice/password/submit", payload)
        dispatch({type: SUBMIT_PASSWORD})
    }


    if (ui.state === UI_STATE_SUBMITTING) {
        return <></>
    }
    if (ui.state === UI_STATE_LOCKED) {
        const wait = formatTimeInterval(ui.waitSeconds)

        return <h4 className="text-warning">
            <strong>
                Time-out: <span className="text-info">{wait}</span><br/>
            </strong>
        </h4>
    }

    return <Terminal terminalState={inputTerminal} submit={enterPassword}/>
}

const PasswordAttempts = () => {
    const attempts = useSelector((state: PasswordRootState) => state.ui.attempts)
    return (
        <ul>
            {attempts.map((attempt, index) => <li key={index}>{attempt}</li>)}
        </ul>
    )
}
