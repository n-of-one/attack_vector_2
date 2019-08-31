import {
    SERVER_SITE_FULL,
    SERVER_ADD_NODE,
    SERVER_MOVE_NODE,
    SERVER_UPDATE_SERVICE,
    SERVER_UPDATE_NETWORK_ID,
    SERVER_ADD_SERVICE,
    SERVER_NODE_UPDATED
} from "../EditorActions";
import {findElementById, updateArrayById} from "../../common/Immutable";
import {SERVER_ICE_HACKED} from "../../hacker/run/model/HackActions";

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
        case SERVER_UPDATE_SERVICE :
            return serverUpdateService(action.data, state);
        case SERVER_ADD_SERVICE :
            return serverAddService(action.data, state);
        case SERVER_NODE_UPDATED :
            return serverRemoveService(action.data.node, state);

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


const serverUpdateService = (update, nodes) => {
    const node = findElementById(nodes, update.nodeId);
    const newServices = updateArrayById(update.service, node.services, update.serviceId);

    const newNodeServices = {services: newServices};

    const newNodes = updateArrayById(newNodeServices, nodes, update.nodeId);
    return newNodes;
};




const serverUpdateNetworkId = (update, nodes) => {
    const newNodeData = {networkId: update.networkId};
    const newNodes = updateArrayById(newNodeData, nodes, update.nodeId);
    return newNodes;
};

const serverAddService = (data, nodes) => {
    const node = findElementById(nodes, data.nodeId);
    const service = data.service;
    const newServices = [...node.services, service];

    const newNodeServices = {services: newServices};

    const newNodes = updateArrayById(newNodeServices, nodes, data.nodeId);
    return newNodes;

};

const serverRemoveService = (node, nodes) => {
    const newNodes = updateArrayById(node, nodes, node.id);
    return newNodes;
};


export { NodesReducer }