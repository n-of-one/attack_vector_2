import React from "react";
import {CONNECTIONS, DISCOVERED, SERVICES, SERVICES_NO_CONNECTIONS, TYPE} from "../../../../common/enums/NodeStatus";
import NodeScanInfoServices from "./NodeScanInfoServices";
import Pad from "../../../../common/component/Pad";

function renderDiscovered() {
    return <>
        No information about services discovered yet.<br/>
        <br/>
        Neighbouring connections not scanned.<br/>
    </>
}

function renderStatusType(node) {
    return <>
        Services discovered: {node.services.length}.<br/>
        <br/>
        Neighbouring connections not scanned.<br/>
    </>
}

function renderStatusConnections(node) {
    const lines = [];
    lines.push(<span key="_0">Layer Service<br/></span>);
    node.services.forEach(service => {
        lines.push(renderServiceIsIce(service))
    });

    return lines;
}

function renderServiceIsIce(service) {
    const text = service.ice ? "ICE" : "service";
    return <span key={service.layer}>
        <Pad p="3" n={service.layer}/>
        <span className="text-primary">{service.layer}</span>
        <Pad p="3" />unknown {text}<br/>
    </span>
}

function renderStatusServicesNoConnections(node) {
    return <>
        <NodeScanInfoServices node={node}/>
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
        case SERVICES_NO_CONNECTIONS:
            return renderStatusServicesNoConnections(node);
        case SERVICES:
            return <NodeScanInfoServices node={node}/>;
        default:
            return renderError(node, status);
    }
};
