import React from 'react';
import {connect} from "react-redux";
import Terminal from "../../../../common/terminal/Terminal";
import CloseButton from "../../../../common/component/CloseButton";
import {FINISH_HACKING_ICE} from "../../model/HackActions";
import {HIDDEN} from "../IceUiState";

const mapDispatchToProps = (dispatch) => {
    return {
        close: () => dispatch({type: FINISH_HACKING_ICE}),
    }
};
let mapStateToProps = (state) => {
    return {
        ice: state.run.ice.tangle,
        displayTerminal: state.run.ice.displayTerminal,
    };
};

export default connect(mapStateToProps, mapDispatchToProps)(
    ({ice, displayTerminal, close}) => {

        const classHidden = ice.uiState === HIDDEN ? " hidden_alpha" : "";


        return (
            <div className="row untangleIcePanelRow">
                <div className="col-lg-12">
                    <div className="row">
                        <div className="col-lg-12">
                            <CloseButton closeAction={close} />
                            <h4 className="text-success">
                                <strong>
                                    Ice: <span className="text-info">Reva</span><br/>
                                    Strength: <span className="text-info">{ice.strength}</span><br/>
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
                            <Terminal className="displayTerminal" terminal={displayTerminal} height={112}/>
                        </div>
                    </div>
                    <hr style={{borderTopColor: "#300", marginTop: "5px", marginBottom: "5px"}}/>

                    <div className={"row transition_alpha_fast" + classHidden}>
                        <div className="col-lg-12" >
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
        );
    });
