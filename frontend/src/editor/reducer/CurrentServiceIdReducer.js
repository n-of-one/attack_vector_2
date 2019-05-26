import {SELECT_NODE, SELECT_SERVICE, SERVER_ADD_SERVICE, SERVER_REMOVE_SERVICE} from "../EditorActions";
import {findElementById} from "../../common/Immutable";

export default (state = null, action, currentNodeId, nodes) => {
    switch(action.type) {
        case SELECT_NODE : return selectServiceFromSelectNode(action.data, nodes);
        case SERVER_ADD_SERVICE : return action.data.service.id;
        case SELECT_SERVICE: return action.serviceId;
        case SERVER_REMOVE_SERVICE: return selectServiceFromSelectNode(currentNodeId, nodes, action.data.nextLayer);
        default: return state;
    }
}

const selectServiceFromSelectNode = (nodeId, nodes, nextLayer) => {
    if (nodeId === null) {
        return null;
    }
    const node = findElementById(nodes, nodeId);

    const layerToFind = (nextLayer) ? nextLayer : node.services.length - 1;

    const serviceToSelect = node.services.find( service => service.layer === layerToFind );
    return serviceToSelect.id;
};



