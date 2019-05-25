import {SELECT_NODE} from "../EditorActions";
import {findElementById} from "../../common/Immutable";

export default (state = null, action, nodes) => {
    switch(action.type) {
        case SELECT_NODE : return selectServiceFromSelectNode(action.data, nodes);
        default: return state;
    }
}

const selectServiceFromSelectNode = (nodeId, nodes) => {
    if (nodeId === null) {
        return null;
    }
    const node = findElementById(nodes, nodeId);

    const highestLayerService = node.services.reduce( (prev, current) => {
        return (prev.layer > current.layer) ? prev : current
    });
    return highestLayerService.id;
};



