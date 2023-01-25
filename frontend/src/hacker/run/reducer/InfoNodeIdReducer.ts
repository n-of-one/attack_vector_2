import {DISPLAY_NODE_INFO, HIDE_NODE_INFO} from "../model/ScanActions";
import {AnyAction} from "redux";

const defaultState = null;

export const infoNodeIdReducer = (state: string | null  = defaultState, action: AnyAction) => {
    switch (action.type) {
        case DISPLAY_NODE_INFO:
            return action.nodeId;
        case HIDE_NODE_INFO:
            return null;
        default:
            return state;
    }
};
