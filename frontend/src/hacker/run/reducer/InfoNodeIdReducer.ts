import {AnyAction} from "redux";

export const DISPLAY_NODE_INFO = "DISPLAY_NODE_INFO"
export const HIDE_NODE_INFO = "HIDE_NODE_INFO"

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
