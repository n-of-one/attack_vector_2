import {AnyAction} from "redux";
export const NAVIGATE_PAGE = "NAVIGATE_PAGE";

const defaultState = "No page yet.";

export const pageReducer = (state = defaultState, action: AnyAction) => {
    switch(action.type) {
        case NAVIGATE_PAGE : return action.to;
        default: return state;
    }
}
