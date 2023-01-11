import React from 'react';
import {connect} from "react-redux";
import runCanvas from "../RunCanvas";
import {findElementById} from "../../../../common/Immutable";
import NodeScanInfoByStatus from "./NodeScanInfoByStatus";
import {CONNECTIONS, DISCOVERED, LAYERS, LAYERS_NO_CONNECTIONS, TYPE} from "../../../../common/enums/NodeStatus";
import Pad from "../../../../common/component/Pad";

const mapDispatchToProps = (dispatch) => {
    return {
        dismiss: () => runCanvas.unSelect(),
    }
};

let mapStateToProps = (state) => {
    const node = findElementById(state.run.site.nodes, state.run.infoNodeId);
    const status = state.run.scan.nodeScanById[node.id].status;
    return {
        node: node,
        status: status,
    };
};


const statusText = (status) => {
    switch (status) {
        case DISCOVERED:
            return "0/3";
        case TYPE:
            return "1/3";
        case CONNECTIONS:
            return "2/3";
        case LAYERS_NO_CONNECTIONS:
            return "2/3";
        case LAYERS:
            return "3/3 (complete)";
        default:
            return "status unknown: " + status;
    }
};


export default connect(mapStateToProps, mapDispatchToProps)(
    ({node, status, dismiss}) => {

        return (
            <>
                <div className="row nodeInfo text" id="scanInfo">
                    <div className="col-lg-12">
                        <div className="row">&nbsp;</div>
                        <div className="row">
                            <div className="col-lg-12">
                            </div>
                        </div>
                        <div className="row">
                            <div className="col-lg-12">
                                <div className="d-flex justify-content-between">
                                    <div className="">
                                        Scan info on <span className="networkId">{node.networkId}</span><Pad p="10" t={node.networkId}/>
                                        <span className="text-muted">scan progress: {statusText(status)}</span><br/>
                                        <br/>
                                        <NodeScanInfoByStatus node={node} status={status}/>
                                    </div>

                                    <div className="nodeInfoClose text-end" onClick={() => dismiss()}>
                                        <span className="close-x-position glyphicon glyphicon-remove"/>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </>
        );
    });
