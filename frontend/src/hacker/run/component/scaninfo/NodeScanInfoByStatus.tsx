import React from "react"
import {CONNECTIONS, DISCOVERED, LAYERS, LAYERS_NO_CONNECTIONS, TYPE} from "../../../../common/enums/NodeStatus"
import {NodeScanInfoLayers} from "./NodeScanInfoLayers"
import {Pad} from "../../../../common/component/Pad"
import {NodeStatus} from "../../reducer/ScanReducer"
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
        <Pad length={3} />unknown {text}<br/>
    </span>
}

function renderStatusLayersNoConnections(node: NodeI) {
    return <>
        <NodeScanInfoLayers node={node}/>
        <br/>
        Neighbouring connections not scanned.<br/>
    </>}

function renderError(node: NodeI, status: NodeStatus) {
    return <>Unknown node status: {status} for node: {node.id}.<br/></>
}

export const NodeScanInfoByStatus = ({node, status}: {node: NodeI, status: NodeStatus}) => {
    switch (status) {
        case DISCOVERED:
            return renderDiscovered()
        case TYPE:
            return renderStatusType(node)
        case CONNECTIONS:
            return renderStatusConnections(node)
        case LAYERS_NO_CONNECTIONS:
            return renderStatusLayersNoConnections(node)
        case LAYERS:
            return <NodeScanInfoLayers node={node}/>
        default:
            return renderError(node, status)
    }
}
