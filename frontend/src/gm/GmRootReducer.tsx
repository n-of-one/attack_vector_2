import {pageReducer} from "../common/menu/pageReducer";
import {SiteInfo, sitesReducer} from "../common/sites/SitesReducer";
import {combineReducers} from "redux";
import {editUserReducer, userOverviewReducer} from "../common/users/UsersReducer";
import {currentUserReducer, GenericUserRootState, User} from "../common/users/UserReducer";
import {Task, tasksReducer} from "./taskmonitor/TaskReducer";


export const usersReducer = combineReducers({
    overview: userOverviewReducer,
    edit: editUserReducer,
})

export interface Users {
    overview: Array<User>,
    edit: User | null
}


export interface GmState extends GenericUserRootState {
    currentPage: string,
    users: Users,
    sites: Array<SiteInfo>,
    currentUser: User | null,
    tasks: Array<Task>
}

export const gmRootReducer = combineReducers({
    currentPage: pageReducer,
    users: usersReducer,
    sites: sitesReducer,
    currentUser: currentUserReducer,
    tasks: tasksReducer,
})
