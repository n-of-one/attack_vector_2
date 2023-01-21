import {DRAG_DROP_START} from "../EditorActions";
import {AnyAction} from "redux";

export interface DragAndDropState {

}

export const defaultDragAndDropState: DragAndDropState = {
    type: "RESOURCE",
    dx: 0,
    dy: 0,
    ice: false
}

const dragAndDropReducer = (state: DragAndDropState, action: AnyAction) => {
    switch (action.type) {
        case DRAG_DROP_START :
            return action.data;
        default:
            return state;
    }
}

export default dragAndDropReducer;
