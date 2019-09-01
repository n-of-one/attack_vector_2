import {SELECT_NODE, SELECT_LAYER, SERVER_ADD_LAYER, SERVER_NODE_UPDATED} from "../EditorActions";
import {findElementById} from "../../common/Immutable";

export default (state = null, action, currentNodeId, nodes) => {
    switch(action.type) {
        case SELECT_NODE : return selectLayerFromSelectNode(action.data, nodes);
        case SERVER_ADD_LAYER : return action.data.layer.id;
        case SELECT_LAYER: return action.layerId;
        case SERVER_NODE_UPDATED: return serverNodeUpdated(action.data.layerId, state);
        default: return state;
    }
}

const selectLayerFromSelectNode = (nodeId, nodes) => {
    if (nodeId === null) {
        return null;
    }
    const node = findElementById(nodes, nodeId);

    const layerToFind = node.layers.length - 1;
    const layerToSelect = node.layers[layerToFind];
    return layerToSelect.id;
};

const serverNodeUpdated = (layerId, state) => {
    return layerId ? layerId : state;
};



