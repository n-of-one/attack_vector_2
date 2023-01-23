import {AnyAction} from "redux"

export const DRAG_DROP_START = "DRAG_DROP_START"

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

export const dragAndDropReducer = (state: DragAndDropState, action: AnyAction) => {
    switch (action.type) {
        case DRAG_DROP_START :
            return action.data
        default:
            return state
    }
}
