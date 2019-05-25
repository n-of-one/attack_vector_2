import {SELECT_NODE} from "../EditorActions";

const CurrentNodeReducer = (state = null, action, nodes) => {
    switch(action.type) {
        case SELECT_NODE : return selectNode(action.data, nodes);
        default: return state;

    }
};

const selectNode = (nodeId, nodes) => {
    const currentNode = nodes.find( node => node.id === nodeId );
    return currentNode ? currentNode : null;
};




export { CurrentNodeReducer, selectNode }