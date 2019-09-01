import {
    SERVER_ADD_NODE,
    SERVER_ADD_LAYER,
    SERVER_MOVE_NODE,
    SERVER_NODE_UPDATED,
    SERVER_SITE_FULL,
    SERVER_UPDATE_NETWORK_ID,
    SERVER_UPDATE_LAYER
} from "../EditorActions";
import {findElementById, updateArrayById} from "../../common/Immutable";

const NodesReducer = (state = [], action) => {
    switch (action.type) {
        case SERVER_SITE_FULL:
            return action.data.nodes;
        case SERVER_ADD_NODE:
            return addNode(action.data, state);
        case SERVER_MOVE_NODE:
            return moveNode(action.data, state);
        case SERVER_UPDATE_NETWORK_ID:
            return serverUpdateNetworkId(action.data, state);
        case SERVER_UPDATE_LAYER :
            return serverUpdateLayer(action.data, state);
        case SERVER_ADD_LAYER :
            return serverAddLayer(action.data, state);
        case SERVER_NODE_UPDATED :
            return serverRemoveLayer(action.data.node, state);

        default:
            return state;
    }
};

const addNode = (data, nodeList) => {
    const node = {...data, connections: []};
    return [...nodeList, node];
};


const moveNode = (data, nodeList) => {
    const newNodeData = {x: data.x, y: data.y};
    return updateArrayById(newNodeData, nodeList, data.nodeId);
};


const serverUpdateLayer = (update, nodes) => {
    const node = findElementById(nodes, update.nodeId);
    const newLayers = updateArrayById(update.layer, node.layers, update.layerId);

    const newNodeLayers = {layers: newLayers};

    const newNodes = updateArrayById(newNodeLayers, nodes, update.nodeId);
    return newNodes;
};




const serverUpdateNetworkId = (update, nodes) => {
    const newNodeData = {networkId: update.networkId};
    const newNodes = updateArrayById(newNodeData, nodes, update.nodeId);
    return newNodes;
};

const serverAddLayer = (data, nodes) => {
    const node = findElementById(nodes, data.nodeId);
    const layer = data.layer;
    const newLayers = [...node.layers, layer];

    const newNodeLayers = {layers: newLayers};

    const newNodes = updateArrayById(newNodeLayers, nodes, data.nodeId);
    return newNodes;

};

const serverRemoveLayer = (node, nodes) => {
    const newNodes = updateArrayById(node, nodes, node.id);
    return newNodes;
};


export { NodesReducer }