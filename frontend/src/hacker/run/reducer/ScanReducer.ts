import {SERVER_DISCOVER_NODES, SERVER_SCAN_FULL, SERVER_UPDATE_NODE_STATUS} from "../model/ScanActions";
import {DISCOVERED} from "../../../common/enums/NodeStatus";
import {AnyAction} from "redux";

export interface Scan {
    runId: string,
    initiatorId: string,
    siteId: string,
    totalDistanceScanned: number,
    startTime: string,
    duration?: number,
    efficiency?: number,
    nodeScanById: NodeScanById
}

export interface NodeScan {
    status: string,
    distance?: number
}

interface NodeScanById { [key: string] : NodeScan }

const defaultState = {
    runId: "",
    initiatorId: "",
    siteId: "",
    totalDistanceScanned: 0,
    startTime: "",
    nodeScanById: {}
}

export const scanReducer = (state: Scan = defaultState, action: AnyAction) => {
    switch (action.type) {
        case SERVER_SCAN_FULL:
            return action.data.scan;
        case SERVER_UPDATE_NODE_STATUS:
            return updateNodeStatus(state, action.data);
        case SERVER_DISCOVER_NODES:
            return updateDiscoveredNodes(state, action.data);
        default:
            return state;
    }
}

interface UpdateNodeStatus {
    nodeId: string,
    newStatus: string
}

const updateNodeStatus = (scan: Scan, {nodeId, newStatus}: UpdateNodeStatus) => {
    const newNodeScanById = updateNodeScanById(scan.nodeScanById, nodeId, newStatus);
    return {...scan, nodeScanById: newNodeScanById};
};

const updateNodeScanById = (nodeScanById: NodeScanById, nodeId: string, newStatus: string) => {
    const newNodeScanById = {...nodeScanById};
    newNodeScanById[nodeId] = { status: newStatus, distance: nodeScanById[nodeId].distance};
    return newNodeScanById;
};

interface DiscoverNodes {
    nodeIds: string[],
    connectionIds: string[]
}

const updateDiscoveredNodes = (scan: Scan, discoverNodes: DiscoverNodes) => {
    let intermediateNodeScanById = scan.nodeScanById;
    discoverNodes.nodeIds.forEach((nodeId: string) => {
        intermediateNodeScanById = updateNodeScanById(intermediateNodeScanById, nodeId, DISCOVERED);
    });
    return {...scan, nodeScanById: intermediateNodeScanById};
};
