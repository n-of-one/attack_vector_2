import React from 'react'
import {useSelector} from "react-redux"
import {Terminal} from "../../../../common/terminal/Terminal"
import {IceTitle} from "../../common/IceTitle";
import {HIDDEN} from "../../common/IceModel";
import {TarRootState} from "../reducer/TarRootReducer";
import {IceStrength} from "../../../../common/model/IceStrength";
import {CloseTabButton} from "../../common/CloseTabButton";

/* eslint jsx-a11y/alt-text: 0*/


const DisplayTerminal = () => {
    const displayTerminal = useSelector( (rootState:TarRootState) => rootState.displayTerminal )

    return <Terminal terminalState={displayTerminal} height={84} />
}


export const TarHome = () => {

    const uiState = useSelector( (rootState:TarRootState) => rootState.iceState.uiState )
    const strength: IceStrength = useSelector( (rootState:TarRootState) => rootState.iceState.strength )

    const classShowCanvas = (uiState === HIDDEN) ? " hidden_alpha" : ""

    return (
        <div className="row icePanelRow">
            <div className="col-lg-12">
                <div className="row">
                    <div className="col-lg-3">
                        <IceTitle name="Tar" strength={strength}/>
                    </div>
                    <div className="col-lg-8" style={{paddingTop: "4px"}}>
                        <DisplayTerminal/>
                    </div>
                    <div className="col-lg-1">
                        <div className="float-end">
                            <CloseTabButton/>
                        </div>
                    </div>
                </div>

                <div className={"row transition_alpha_fast" + classShowCanvas}>
                    <div className="col-lg-12">
                        <div>
                            <canvas id="tarCanvas" style={{
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
