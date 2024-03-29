import React from 'react'
import {useSelector} from "react-redux"
import {Terminal} from "../../../../common/terminal/Terminal"
import {TangleRootState} from "../reducer/TangleRootReducer";
import {HIDDEN} from "../../common/IceModel";
import {IceTitle} from "../../common/IceTitle";

export const TangleIceHome = () => {

    const ice = useSelector( (state:TangleRootState) => state.tangle )
    const displayTerminal = useSelector( (state:TangleRootState) => state.displayTerminal )

    const classHidden = ice.uiState === HIDDEN ? " hidden_alpha" : ""


    return (
        <div className="row untangleIcePanelRow">
            <div className="col-lg-12">
                <div className="row">
                    <div className="col-lg-12">
                        <IceTitle name="Gaanth" strength={ice.strength} />
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
                    <div className="col-lg-12">
                        <div>
                            <canvas id="untangleCanvas" style={{
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
