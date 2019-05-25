import {SERVER_SITE_FULL, SERVER_ADD_NODE, SERVER_MOVE_NODE, SERVER_UPDATE_SERVICE_DATA, SERVER_UPDATE_NETWORK_ID} from "../EditorActions";
import {findElementById, updateArray} from "../../common/Immutable";

const NodesReducer = (state = [], action) => {
    switch (action.type) {
        case SERVER_SITE_FULL:
            return action.data.nodes;
        case SERVER_ADD_NODE:
            return addNode(action.data, state);
        case SERVER_MOVE_NODE:
            return moveNode(action.data, state);
        case SERVER_UPDATE_NETWORK_ID:
            return serverUpdateNetworkId(action.data ,state);
        case SERVER_UPDATE_SERVICE_DATA :
            return serverUpdateServiceData(action.data, state);

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
    return updateArray(newNodeData, nodeList, data.nodeId);
};


const serverUpdateServiceData = (update, nodes) => {
    const node = findElementById(nodes, update.nodeId);
    const service = findElementById(node.services, update.serviceId);
    const newService = createUpdatedServiceData(update, service);
    const newServices = updateArray(newService, node.services, update.serviceId);

    const newNodeServices = {services: newServices};

    const newNodes = updateArray(newNodeServices, nodes, update.nodeId);
    return newNodes;
};


const createUpdatedServiceData = (update, service) => {
    if (update.serviceId !== service.id) {
        return service;
    }
    const newData = {...service.data};
    newData[update.key] = update.value;
    const newService = {...service, data: newData};
    return newService;
};

const serverUpdateNetworkId = (update, nodes) => {
    const newNodeData = {networkId: update.networkId};
    const newNodes = updateArray(newNodeData, nodes, update.nodeId);
    return newNodes;
};




export { NodesReducer, createUpdatedServiceData }