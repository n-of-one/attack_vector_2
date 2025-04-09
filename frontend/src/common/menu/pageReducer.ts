import {AnyAction} from "redux";


export enum Page {
    LOADING = "LOADING",

    HACKER_HOME = "HACKER_HOME",
    SITES = "SITES",
    RUN = "RUN",
    HACKER_SCRIPTS = "HACKER_SCRIPTS",
    ME = "ME",
    ICE = "ICE",
    ICE_AUTH = "ICE_AUTH",

// Gm & admin pages
    USERS = "USERS",
    CONFIG = "CONFIG",
    TASKS = "TASKS",
    STATISTICS = "STATISTICS",
    GM_SCRIPTS_HOME = "GM_SCRIPTS_HOME",
    SCRIPT_TYPE_MANAGEMENT = "SCRIPT_TYPE_MANAGEMENT",
    SCRIPT_MANAGEMENT = "SCRIPT_MANAGEMENT",
    SCRIPT_ACCESS_MANAGEMENT = "SCRIPT_ACCESS_MANAGEMENT",

// Display this page when another tab was opened for the same hacker / app connection.
    FORCE_DISCONNECT = "FORCE_DISCONNECT",

}


// ACTIONS:
export const NAVIGATE_PAGE = "NAVIGATE_PAGE";


export const pageReducer = (state: Page = Page.LOADING, action: AnyAction): Page => {
    switch (action.type) {
        case NAVIGATE_PAGE :
            return action.to;
        default:
            return state;
    }
}
