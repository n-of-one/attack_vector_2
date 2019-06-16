import React from 'react';
import {connect} from "react-redux";
import Terminal from "../../../common/terminal/Terminal";
import ScanCanvasPanel from "./ScanCanvasPanel";
import NodeScanInfo from "./scaninfo/NodeScanInfo";

const mapDispatchToProps = (dispatch) => {
    return {
        dispatch: dispatch,
    }
};
let mapStateToProps = (state) => {
    return {
        terminal: state.terminal,
        messageTerminal: state.run.messageTerminal,
        infoNodeId: state.run.infoNodeId,
    };
};



export default connect(mapStateToProps, mapDispatchToProps)(
    ({terminal, messageTerminal, infoNodeId, dispatch}) => {

        return (
            <div className="row">
                <div className="col-lg-6">
                        <NodeScanInfo />
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
