import {DRAG_DROP_START} from "../EditorActions";

const dragAndDropReducer = (state = { type: "RESOURCE", dx: 0, dy: 0, ice: false }, action) => {
    switch(action.type) {
        case DRAG_DROP_START : return action.data;
        default: return state;
    }
}

export default dragAndDropReducer;
