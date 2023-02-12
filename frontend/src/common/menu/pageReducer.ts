import {AnyAction} from "redux";


export const HACKER_HOME = "HACKER_HOME";
export const RUN = "RUN";
export const MAIL = "MAIL";
export const SCRIPTS = "SCRIPTS";
export const HACKER_COMMUNITY = "HACKER_COMMUNITY";
export const ME = "ME";

// ACTIONS:
export const NAVIGATE_PAGE = "NAVIGATE_PAGE";


const defaultState = "No page yet.";

export const pageReducer = (state: string = defaultState, action: AnyAction): string => {
    switch(action.type) {
        case NAVIGATE_PAGE : return action.to;
        default: return state;
    }
}
