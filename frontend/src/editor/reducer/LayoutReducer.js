import {SERVER_ADD_NODE, SERVER_SITE_FULL} from "../EditorActions";

const defaultLayout = {
    id: "unknown",
    nodeIds: [],
    connectionIds: []
};

export default (state = defaultLayout, action) => {
    switch(action.type) {
        case SERVER_SITE_FULL: return action.data.layout;
        case SERVER_ADD_NODE: return addNode(action.data, state);
        default: return state;
    }
}

let addNode = (data, state) => {
    let newNodeIds = [...state.nodeIds, data.id];
    return { ...state, nodeIds: newNodeIds };
};
