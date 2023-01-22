import {SELECT_NODE, SERVER_SITE_FULL} from "../EditorActions";
import {AnyAction} from "redux";

const currentNodeIdReducer = (state: string | null = null, action: AnyAction) => {
    switch(action.type) {
        case SELECT_NODE : return action.data;
        case SERVER_SITE_FULL: return null;
        default: return state;

    }
};

export default currentNodeIdReducer;
