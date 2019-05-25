import {SELECT_NODE} from "../EditorActions";
import {selectNode} from "./CurrentNodeReducer";

/*
Sample state

const sampleState = {
    id: "site-2e76-43ab-serv-418c",
    type: "OS",
    layer: 0,
    data: {
        "name": "Entrace",
        "gmNote": "This is the start node for the hacker"
    }
};

This state is just a reference to the current selected service of the current node.

 */

export default (state = null, action, nodes) => {
    switch(action.type) {
        case SELECT_NODE : return selectServiceFromSelectNode(action.data, nodes);
        default: return state;
    }
}

const selectServiceFromSelectNode = (nodeId, nodes) => {
    const currentNode = selectNode(nodeId, nodes);
    if (!currentNode) {
        return null;
    }

    const highestLayerService = currentNode.services.reduce( (prev, current) => {
        return (prev.layer > current.layer) ? prev : current
    });
    return highestLayerService;
};

