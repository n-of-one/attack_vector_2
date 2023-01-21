import {SERVER_ADD_NODE, SERVER_SITE_FULL} from "../EditorActions";
import {AnyAction} from "redux";

export interface Layout {
    id: string,
    nodeIds: Array<string>,
    connectionIds: Array<string>
}

export interface AddNodeData {
    id: string
}

export const defaultLayout: Layout = {
    id: "unknown",
    nodeIds: [],
    connectionIds: []
};

const layoutReducer = (state: Layout = defaultLayout, action: AnyAction) => {
    switch(action.type) {
        case SERVER_SITE_FULL: return action.data.layout;
        case SERVER_ADD_NODE: return addNode(action.data as AddNodeData, state);
        default: return state;
    }
}

let addNode = (data: AddNodeData, state: Layout) => {
    let newNodeIds = [...state.nodeIds, data.id];
    return { ...state, nodeIds: newNodeIds };
};

export default layoutReducer;
