import {SELECT_NODE} from "../EditorActions";
import {AnyAction} from "redux";

const currentNodeIdReducer = (state: string | null = null, action: AnyAction) => {
    switch(action.type) {
        case SELECT_NODE : return action.data;
        default: return state;

    }
};

export default currentNodeIdReducer;
