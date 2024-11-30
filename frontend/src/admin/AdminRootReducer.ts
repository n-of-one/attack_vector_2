import {currentUserReducer} from "../common/users/CurrentUserReducer";
import {configReducer} from "./config/ConfigReducer";
import {combineReducers} from "redux";
import {pageReducer} from "../common/menu/pageReducer";
import {GmRootState, scriptManagementReducer, usersReducer} from "../gm/GmRootReducer";
import {Task, tasksReducer} from "../gm/taskmonitor/TaskReducer";
import {sitesReducer} from "../common/sites/SitesReducer";

export interface AdminRootState extends GmRootState {
    tasks: Task[]
}

export const adminRootReducer = combineReducers({
    // from GenericUserRootState
    currentUser: currentUserReducer,
    config: configReducer,

    // from GmRootState
    currentPage: pageReducer,
    users: usersReducer,
    sites: sitesReducer,
    scriptsManagement: scriptManagementReducer,

    // from AdminRootState
    tasks: tasksReducer,
})

