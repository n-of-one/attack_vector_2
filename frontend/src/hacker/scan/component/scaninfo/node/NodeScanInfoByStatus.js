import React from "react";
import {CONNECTIONS, DISCOVERED, SERVICES, TYPE} from "../../../../../common/enums/NodeStatus";
import NodeScanInfoServices from "./NodeScanInfoServices";

function renderDiscovered() {
    return <>
        No information about services discovered yet.<br/>
        <br/>
        Neighbouring connections not scanned.
    </>
}

function renderStatusType(node) {
    return <>
        {node.services.length} services discovered.<br/>
        <br/>
        Neighbouring connections not scanned.
    </>
}

function renderStatusConnections(node) {
    const lines = [];
    node.services.forEach(service => {
        lines.push(renderServiceIsIce(service))
    });

    return lines;
}

function renderServiceIsIce(service) {
    const text = service.ice ? "ICE" : "service";
    return <>{service.layer}&nbsp;&nbsp;unknown {text}<br/></>
}

function renderError(node, status) {
    return <>Unknown node status: {status} for node: {node.id}</>

}

export default ({node, status}) => {
    switch (status) {
        case DISCOVERED:
            return renderDiscovered();
        case TYPE:
            return renderStatusType(node);
        case CONNECTIONS:
            return renderStatusConnections(node);
        case SERVICES:
            return <NodeScanInfoServices node={node}/>;
        default:
            return renderError(node, status);
    }
};
