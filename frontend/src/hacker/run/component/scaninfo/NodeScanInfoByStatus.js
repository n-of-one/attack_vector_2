import React from "react";
import {CONNECTIONS, DISCOVERED, SERVICES, TYPE} from "../../../../common/enums/NodeStatus";
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
    lines.push(<>Layer Service<br/></>);
    node.services.forEach(service => {
        lines.push(renderServiceIsIce(service))
    });

    return lines;
}

function renderServiceIsIce(service) {
    const text = service.ice ? "ICE" : "service";
    return <>
        <Pad p="2"/>
        <span className="text-primary">{service.layer}</span>
        <Pad p="4" n={service.layer}/>unknown {text}<br/>
    </>
}

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
        case SERVICES:
            return <NodeScanInfoServices node={node}/>;
        default:
            return renderError(node, status);
    }
};
