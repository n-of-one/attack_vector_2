import {AnyAction} from "redux";


export const TEXT_INPUT_CHANGED = "TEXT_INPUT_CHANGED";

export const terminalPreviewReducer = (state: string = "", action: AnyAction): string => {
    switch (action.type) {
        case TEXT_INPUT_CHANGED:
            return action.text
        default:
            return state
    }
}
