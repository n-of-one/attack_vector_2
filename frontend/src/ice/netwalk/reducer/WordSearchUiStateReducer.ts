import {AnyAction} from "redux";

export type UIState = "HIDDEN" | "VISIBLE"

const defaultState: UIState = "HIDDEN"

export const wordSearchUiStateReducer = (state: UIState = defaultState, action: AnyAction): UIState => {

    switch (action.type) {
        case "WORD_SEARCH_BEGIN":
            return "VISIBLE"
        default:
            return state
    }
}