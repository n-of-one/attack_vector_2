import {SELECT_NODE} from "../EditorActions";

export default (state = null, action) => {
    switch(action.type) {
        case SELECT_NODE : return action.data;
        default: return state;

    }
};
