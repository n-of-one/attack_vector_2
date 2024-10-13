import {currentUserReducer} from "../common/users/UserReducer";
import {configReducer} from "./config/ConfigReducer";
import {combineReducers} from "redux";
import {pageReducer} from "../common/menu/pageReducer";
import {GmRootState, usersReducer} from "../gm/GmRootReducer";
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

    // from AdminRootState
    tasks: tasksReducer,
})

