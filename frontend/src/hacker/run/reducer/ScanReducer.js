import {SERVER_DISCOVER_NODES, SERVER_SCAN_FULL, SERVER_UPDATE_NODE_STATUS} from "../model/ScanActions";
import {DISCOVERED} from "../../../common/enums/NodeStatus";

const defaultState = {};

export default (state = defaultState, action) => {
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


const updateNodeStatus = (scan, {nodeId, newStatus}) => {
    const newNodeScanById = updateNodeScanById(scan.nodeScanById, nodeId, newStatus);
    return {...scan, nodeScanById: newNodeScanById};
};

const updateNodeScanById = (nodeScanById, nodeId, newStatus) => {
    const newNodeScanById = {...nodeScanById};
    newNodeScanById[nodeId] = { status: newStatus, distance: nodeScanById[nodeId].distance};
    return newNodeScanById;
};

const updateDiscoveredNodes = (scan, {nodeIds}) => {
    let intermediateNodeScanById = scan.nodeScanById;
    nodeIds.forEach(nodeId => {
        intermediateNodeScanById = updateNodeScanById(intermediateNodeScanById, nodeId, DISCOVERED);
    });
    return {...scan, nodeScanById: intermediateNodeScanById};
};
