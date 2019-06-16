import {DISPLAY_NODE_INFO, HIDE_NODE_INFO} from "../model/ScanActions";

const defaultState = null;

const infoNodeIdReducer = (state = defaultState, action) => {
    switch (action.type) {
        case DISPLAY_NODE_INFO:
            return action.nodeId;
        case HIDE_NODE_INFO:
            return null;
        default:
            return state;
    }
};


export default infoNodeIdReducer