import {DRAG_DROP_START} from "../EditorActions";
import {AnyAction} from "redux";

export interface DragAndDropState {
    type: string,
    dx: number,
    dy: number,
    ice: boolean
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
