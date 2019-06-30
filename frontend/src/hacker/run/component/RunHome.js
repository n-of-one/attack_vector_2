import React from 'react';
import {connect} from "react-redux";
import Terminal from "../../../common/terminal/Terminal";
import ScanCanvasPanel from "./RunCanvasPanel";
import NodeScanInfo from "./scaninfo/NodeScanInfo";

const mapDispatchToProps = (dispatch) => {
    return {
        dispatch: dispatch,
    }
};
let mapStateToProps = (state) => {
    const siteName = (state.run.site.siteData) ? state.run.site.siteData.name : "";
    return {
        terminal: state.terminal,
        messageTerminal: state.run.messageTerminal,
        infoNodeId: state.run.infoNodeId,
        siteName: siteName,
    };
};



export default connect(mapStateToProps, mapDispatchToProps)(
    ({terminal, messageTerminal, infoNodeId, siteName, dispatch}) => {

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
                            <span className="text">Site: {siteName}</span>
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
