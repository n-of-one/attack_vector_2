import {SELECT_NODE} from "../EditorActions";

const currentNodeIdReducer = (state = null, action) => {
    switch(action.type) {
        case SELECT_NODE : return action.data;
        default: return state;

    }
};

export default currentNodeIdReducer;
