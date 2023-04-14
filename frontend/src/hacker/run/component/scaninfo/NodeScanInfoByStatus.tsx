import React from "react"
import {CONNECTIONS_KNOWN_3, DISCOVERED_1, FULLY_SCANNED_4, NodeScanStatus, TYPE_KNOWN_2} from "../../../../common/enums/NodeStatus"
import {NodeScanInfoLayers} from "./NodeScanInfoLayers"
import {Pad} from "../../../../common/component/Pad"
import {LayerDetails, NodeI} from "../../../../editor/reducer/NodesReducer"

function renderDiscovered() {
    return <>
        No information about layers discovered yet.<br/>
        <br/>
        No information about additional neighbouring nodes discovered yet.<br/>
    </>
}

function renderStatusType(node: NodeI) {
    return <>
        {renderLayersAsUnknown(node)}
        <br/>
        No information about additional neighbouring nodes discovered yet.<br/>
    </>
}

function renderLayersAsUnknown(node: NodeI) {
    const lines = []
    lines.push(<span key="_0">Level Layer<br/></span>)
    node.layers.forEach(layer => {
        lines.push(renderLayerIsIce(layer))
    })
    return <>{lines}</>
}
function renderStatusConnections(node: NodeI) {
    return <>
        {renderLayersAsUnknown(node)}
        <br/>
        Neighbouring nodes discovered.<br/>
    </>
}



function renderLayerIsIce(layer: LayerDetails) {
    const text = layer.ice ? "ICE" : "layer"
    return <span key={layer.level}>
        <Pad length={3} numberValue={layer.level}/>
        <span className="text-primary">{layer.level}</span>
        <Pad length={3} /> unknown {text}<br/>
    </span>
}

function renderError(node: NodeI, status: NodeScanStatus) {
    return <>Unknown node status: {status} for node: {node.id}.<br/></>
}

export const NodeScanInfoByStatus = ({node, status}: {node: NodeI, status: NodeScanStatus}) => {
    switch (status) {
        case DISCOVERED_1:
            return renderDiscovered()
        case TYPE_KNOWN_2:
            return renderStatusType(node)
        case CONNECTIONS_KNOWN_3:
            return renderStatusConnections(node)
        case FULLY_SCANNED_4:
            return <NodeScanInfoLayers node={node}/>
        default:
            return renderError(node, status)
    }
}
