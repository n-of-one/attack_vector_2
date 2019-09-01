import React from "react";
import {CONNECTIONS, DISCOVERED, LAYERS, LAYERS_NO_CONNECTIONS, TYPE} from "../../../../common/enums/NodeStatus";
import NodeScanInfoLayers from "./NodeScanInfoLayers";
import Pad from "../../../../common/component/Pad";

function renderDiscovered() {
    return <>
        No information about layers discovered yet.<br/>
        <br/>
        Neighbouring connections not scanned.<br/>
    </>
}

function renderStatusType(node) {
    return <>
        Layers discovered: {node.layers.length}.<br/>
        <br/>
        Neighbouring connections not scanned.<br/>
    </>
}

function renderStatusConnections(node) {
    const lines = [];
    lines.push(<span key="_0">Layer Layer<br/></span>);
    node.layers.forEach(layer => {
        lines.push(renderLayerIsIce(layer))
    });

    return lines;
}

function renderLayerIsIce(layer) {
    const text = layer.ice ? "ICE" : "layer";
    return <span key={layer.layer}>
        <Pad p="3" n={layer.layer}/>
        <span className="text-primary">{layer.layer}</span>
        <Pad p="3" />unknown {text}<br/>
    </span>
}

function renderStatusLayersNoConnections(node) {
    return <>
        <NodeScanInfoLayers node={node}/>
        <br/>
        Neighbouring connections not scanned.<br/>
    </>}

function renderError(node, status) {
    return <>Unknown node status: {status} for node: {node.id}.<br/></>
}

export default ({node, status}) => {
    switch (status) {
        case DISCOVERED:
            return renderDiscovered();
        case TYPE:
            return renderStatusType(node);
        case CONNECTIONS:
            return renderStatusConnections(node);
        case LAYERS_NO_CONNECTIONS:
            return renderStatusLayersNoConnections(node);
        case LAYERS:
            return <NodeScanInfoLayers node={node}/>;
        default:
            return renderError(node, status);
    }
};
