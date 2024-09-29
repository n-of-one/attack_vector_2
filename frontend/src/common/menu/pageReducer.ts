import {AnyAction} from "redux";


// Hacker pages
export const HACKER_HOME = "HACKER_HOME";
export const SITES = "SITES";
export const RUN = "RUN";
export const MAIL = "MAIL";
export const ME = "ME";

// Gm & admin pages
export const USERS = "USERS"
export const CONFIG = "CONFIG"
export const TASKS = "TASKS"

// Display this page when another tab was opened for the same hacker / app connection.
export const FORCE_DISCONNECT = "FORCE_DISCONNECT"



// ACTIONS:
export const NAVIGATE_PAGE = "NAVIGATE_PAGE";


const defaultState = "No page yet.";

export const pageReducer = (state: string = defaultState, action: AnyAction): string => {
    switch(action.type) {
        case NAVIGATE_PAGE : return action.to;
        default: return state;
    }
}
