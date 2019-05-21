import React from 'react';
import {connect} from "react-redux";
import Terminal from "../../../common/terminal/Terminal";
import ScanCanvasPanel from "./ScanCanvasPanel";

/* eslint jsx-a11y/accessible-emoji: 0 */
/* eslint jsx-a11y/anchor-is-valid: 0*/

const mapDispatchToProps = (dispatch) => {
    return {
        dispatch: dispatch,
    }
};
let mapStateToProps = (state) => {
    return {
        terminal: state.terminal,
        messageTerminal: state.scan.messageTerminal,
    };
};




export default connect(mapStateToProps, mapDispatchToProps)(
    ({terminal, messageTerminal, dispatch}) => {

        return (
            <div className="row">
                <div className="col-lg-6">
                    <div className="row backgroundLight">
                        &nbsp;
                    </div>
                    <div className="row">
                        <Terminal terminal={terminal} dispatch={dispatch} height="780px"/>
                    </div>
                </div>
                <div className="col-lg-6 rightPane">

                    <div className="row">
                        <div className="col-lg-12">
                            <span className="text">Scans</span>
                        </div>
                    </div>
                    <div className="row">
                        <div className="col-lg-12">
                            <ScanCanvasPanel />
                        </div>
                    </div>
                </div>
            </div>
        );
    });
