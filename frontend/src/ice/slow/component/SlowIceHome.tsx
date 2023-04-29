import React from 'react'
import {useSelector} from "react-redux"
import {Terminal} from "../../../common/terminal/Terminal"
import {IceTitle} from "../../../common/component/IceTitle";
import {HIDDEN} from "../../IceModel";
import {SlowIceRootState} from "../reducer/SlowIceRootReducer";
import {IceStrength} from "../../../common/model/IceStrength";

/* eslint jsx-a11y/alt-text: 0*/


const DisplayTerminal = () => {
    const displayTerminal = useSelector( (rootState:SlowIceRootState) => rootState.displayTerminal )

    return <Terminal terminalState={displayTerminal} height={84} />
}


export const SlowIceHome = () => {

    const uiState = useSelector( (rootState:SlowIceRootState) => rootState.iceState.uiState )
    const strength: IceStrength = useSelector( (rootState:SlowIceRootState) => rootState.iceState.strength )

    // const classShowCanvas = (uiState === HIDDEN) ? " hidden_alpha" : ""
    const classShowCanvas =  ""

    return (
        <div className="row icePanelRow">
            <div className="col-lg-12">
                <div className="row">
                    <div className="col-lg-3">
                        <IceTitle name="Saag" strength={strength} />
                    </div>
                    <div className="col-lg-9" style={{paddingTop: "4px"}}>
                        <DisplayTerminal />
                    </div>
                </div>

                <div className={"row transition_alpha_fast" + classShowCanvas}>
                    <div className="col-lg-12">
                        <div>
                            <canvas id="slowIceCanvas" style={{
                                "borderRadius": "3px 3px 3px 3px",
                                "marginTop": "10px",
                                "marginBottom": "10px",
                            }}/>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    )
}
