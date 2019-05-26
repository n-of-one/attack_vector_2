import {SELECT_NODE, SELECT_SERVICE, SERVER_ADD_SERVICE, SERVER_NODE_UPDATED} from "../EditorActions";
import {findElementById} from "../../common/Immutable";

export default (state = null, action, currentNodeId, nodes) => {
    switch(action.type) {
        case SELECT_NODE : return selectServiceFromSelectNode(action.data, nodes);
        case SERVER_ADD_SERVICE : return action.data.service.id;
        case SELECT_SERVICE: return action.serviceId;
        case SERVER_NODE_UPDATED: return serverNodeUpdated(action.data.serviceId, state);
        default: return state;
    }
}

const selectServiceFromSelectNode = (nodeId, nodes) => {
    if (nodeId === null) {
        return null;
    }
    const node = findElementById(nodes, nodeId);

    const layerToFind = node.services.length - 1;
    const serviceToSelect = node.services[layerToFind];
    return serviceToSelect.id;
};

const serverNodeUpdated = (serviceId, state) => {
    return serviceId ? serviceId : state;
};



