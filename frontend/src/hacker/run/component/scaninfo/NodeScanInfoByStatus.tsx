import React from "react"
import {NodeScanStatus} from "../../../../common/enums/NodeStatus"
import {NodeScanInfoLayers} from "./NodeScanInfoLayers"
import {NodeI} from "../../../../common/sites/SiteModel";

function renderUnconnectable() {
    return <>
        No information node discovered yet.<br/>
    </>
}
function renderConnectable() {
    return <>
        No information about layers discovered yet.<br/>
        <br/>
        No information about additional neighbouring nodes discovered yet.<br/>
    </>
}

function renderError(node: NodeI, status: NodeScanStatus) {
    return <>Unknown node status: {status} for node: {node.id}.<br/></>
}

export const NodeScanInfoByStatus = ({node, status}: {node: NodeI, status: NodeScanStatus}) => {
    switch (status) {
        case NodeScanStatus.UNCONNECTABLE_1:
            return renderUnconnectable()
        case NodeScanStatus.CONNECTABLE_2:
            return renderConnectable()
        case NodeScanStatus.ICE_PROTECTED_3:
            return <NodeScanInfoLayers node={node} allLayersRevealed={false}/>
        case NodeScanStatus.FULLY_SCANNED_4:
            return <NodeScanInfoLayers node={node} allLayersRevealed={true}/>
        default:
            return renderError(node, status)
    }
}
