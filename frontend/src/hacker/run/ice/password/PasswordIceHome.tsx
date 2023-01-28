import React from 'react'
import {useDispatch, useSelector} from "react-redux"
import Terminal from "../../../../common/terminal/Terminal"
import {HIDDEN, LOCKED} from "../IceUiState"
import CloseButton from "../../../../common/component/CloseButton"
import {FINISH_HACKING_ICE} from "../../model/HackActions"
import serverTime from "../../../../common/ServerTime"
import {PasswordIceI} from "./PasswordIceReducer"
import {HackerState} from "../../../HackerRootReducer"
import {TerminalState} from "../../../../common/terminal/TerminalReducer"
import {Dispatch} from "redux"

export const ICE_PASSWORD_SUBMIT = "ICE_PASSWORD_SUBMIT";


const renderInput = (inputTerminal: TerminalState, enterPassword: (attempt: string) => void, dispatch: Dispatch, ice: PasswordIceI) => {
    if (ice.uiState === LOCKED) {
        return <></>
    }
    if (ice.waitSeconds && ice.waitSeconds > 0) {

        const wait = serverTime.format(ice.waitSeconds)

        return <h4 className="text-warning">
            <strong>
                Time-out: <span className="text-info">{wait}</span><br/>
            </strong>
        </h4>
    }

    return <Terminal terminal={inputTerminal} submit={enterPassword} dispatch={dispatch}/>
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
    const close = () => dispatch({type: FINISH_HACKING_ICE})

    const ice = useSelector((state: HackerState) => state.run.ice.password!)
    const displayTerminal = useSelector((state: HackerState) => state.run.ice.displayTerminal)
    const inputTerminal = useSelector((state: HackerState) => state.run.ice.inputTerminal)

    const enterPassword = (password: string) => dispatch({type: ICE_PASSWORD_SUBMIT, password: password})
    const classHidden = ice.uiState === HIDDEN ? " hidden_alpha" : ""


    return (
        <div className="row passwordIcePanelRow">
            <div className="col-lg-12">
                <div className="row">
                    <div className="col-lg-12">
                        <h4 className="text-success">
                            <strong>
                                Ice: <span className="text-info">Aruna</span>&nbsp;<CloseButton closeAction={close}/><br/>
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