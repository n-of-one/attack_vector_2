import React from 'react';
import {connect} from "react-redux";
import scanCanvas from "./ScanCanvas";
import {findElementById} from "../../../common/Immutable";
import NodeScanInfoByStatus from "./scaninfo/node/NodeScanInfoByStatus";

const mapDispatchToProps = (dispatch) => {
    return {
        dismiss: () => scanCanvas.unselect(),
    }
};

let mapStateToProps = (state) => {


    if (state.scan.infoNodeId) {
        const node = findElementById(state.scan.site.nodes, state.scan.infoNodeId);
        const status = state.scan.scan.nodeScanById[node.id].status;
        return {
            node: node,
            status: status,
        };
    }

    return {
        node: null,
    };
};

export default connect(mapStateToProps, mapDispatchToProps)(
    ({node, status, dismiss}) => {

        if (!node) {
            return <div className="row backgroundLight">&nbsp;</div>
        }

        return (
            <>
                <div className="row backgroundLight">&nbsp;</div>
                <div className="row nodeInfo text" id="scanInfo">
                    <div className="nodeInfoClose" onClick={() => dismiss()}><span className="glyphicon glyphicon-ok"/></div>
                    <div className="col-lg-12">
                        <br />
                        Scan info on <span className="networkId">{node.networkId}</span><br />
                        <br />
                        <NodeScanInfoByStatus node={node} status={status}/>
                    </div>
                </div>
            </>
        );
    });
