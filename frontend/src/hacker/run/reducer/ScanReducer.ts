import {SERVER_DISCOVER_NODES} from "../model/ScanActions";
import {NodeScanStatus} from "../../../common/enums/NodeStatus";
import {AnyAction} from "redux";
import {ProbeResultConnections, SERVER_ENTER_RUN, SERVER_UPDATE_NODE_STATUS} from "../../server/RunServerActionProcessor";

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
    status: NodeScanStatus,
    distance?: number
}


export interface UpdateNodeStatusAction {
    nodeId: string,
    newStatus: NodeScanStatus
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
        case SERVER_ENTER_RUN:
            return action.data.run;
        case SERVER_UPDATE_NODE_STATUS:
            return updateNodeStatus(state, action.data);
        case SERVER_DISCOVER_NODES:
            return updateDiscoveredNodes(state, action.data);
        default:
            return state;
    }
}



const updateNodeStatus = (scan: Scan, {nodeId, newStatus}: UpdateNodeStatusAction) => {
    const newNodeScanById = updateNodeScanById(scan.nodeScanById, nodeId, newStatus);
    return {...scan, nodeScanById: newNodeScanById};
};

const updateNodeScanById = (nodeScanById: NodeScanById, nodeId: string, newStatus: NodeScanStatus) => {
    const newNodeScanById = {...nodeScanById};
    newNodeScanById[nodeId] = { status: newStatus, distance: nodeScanById[nodeId].distance};
    return newNodeScanById;
};


const updateDiscoveredNodes = (scan: Scan, probeResultConnections: ProbeResultConnections) => {
    let intermediateNodeScanById = scan.nodeScanById;

    Object.entries(probeResultConnections.nodeStatusById).forEach(([nodeId, status]) => {
        intermediateNodeScanById = updateNodeScanById(intermediateNodeScanById, nodeId, status as NodeScanStatus)
    })
    return {...scan, nodeScanById: intermediateNodeScanById};
};
