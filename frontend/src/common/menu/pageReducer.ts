import {AnyAction} from "redux";


// Hacker pages
export const HACKER_HOME = "HACKER_HOME";
export const RUN = "RUN";
export const MAIL = "MAIL";
export const SCRIPTS = "SCRIPTS";
export const HACKER_COMMUNITY = "HACKER_COMMUNITY";
export const ME = "ME";

// Gm & admin pages
export const GM_SITES = "GM_SITES"
export const LOGS = "LOGS"
export const MISSIONS = "MISSIONS"
export const USERS = "USERS"
export const ADMIN = "ADMIN"

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
