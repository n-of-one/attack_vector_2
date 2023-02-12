import React from 'react'
import {useSelector} from "react-redux"
import {runCanvas} from "../RunCanvas"
import {findElementById} from "../../../../common/Immutable"
import {NodeScanInfoByStatus} from "./NodeScanInfoByStatus"
import {CONNECTIONS_KNOWN_3, DISCOVERED_1, FULLY_SCANNED_4, NodeScanStatus, TYPE_KNOWN_2} from "../../../../common/enums/NodeStatus"
import {Pad} from "../../../../common/component/Pad"
import {HackerState} from "../../../HackerRootReducer"
import {NodeI} from "../../../../editor/reducer/NodesReducer";

const statusText = (status: NodeScanStatus) => {
    switch (status) {
        case DISCOVERED_1:
            return "0/3"
        case TYPE_KNOWN_2:
            return "1/3"
        case CONNECTIONS_KNOWN_3:
            return "2/3"
        case FULLY_SCANNED_4:
            return "3/3 (complete)"
        default:
            return "status unknown: " + status
    }
}

const stateSelector = (state: HackerState) => {
    if (state.run.infoNodeId === null) {
        return {node: null, status: null}
    }
    const node = findElementById(state.run.site.nodes, state.run.infoNodeId)
    const status = state.run.scan.nodeScanById[node.id].status
    return {
        node: node,
        status: status,
    }
}


export const NodeScanInfo = () => {

    const {node, status}: { node: NodeI | null, status: NodeScanStatus | null } = useSelector(stateSelector)

    if (node === null || status === null) {
        return <></>
    }

    const dismiss = () => {
        runCanvas.unSelect()
    }

    return (
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
                                Scan info on <span className="networkId">{node.networkId}</span><Pad length={10} textValue={node.networkId}/>
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
    )
}
