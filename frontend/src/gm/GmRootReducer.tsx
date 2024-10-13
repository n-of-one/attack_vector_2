import {pageReducer} from "../common/menu/pageReducer";
import {SiteInfo, sitesReducer} from "../common/sites/SitesReducer";
import {combineReducers} from "redux";
import {editUserReducer, userOverviewReducer} from "../common/users/UsersReducer";
import {currentUserReducer, GenericUserRootState, User} from "../common/users/UserReducer";
import {configReducer} from "../admin/config/ConfigReducer";


export const usersReducer = combineReducers({
    overview: userOverviewReducer,
    edit: editUserReducer,
})

export interface Users {
    overview: Array<User>,
    edit: User | null
}


export interface GmRootState extends GenericUserRootState {
    currentPage: string,
    users: Users,
    sites: Array<SiteInfo>,
}

export const gmRootReducer = combineReducers({
    // from GenericUserRootState
    currentUser: currentUserReducer,
    config: configReducer,

    // from GmRootState
    currentPage: pageReducer,
    users: usersReducer,
    sites: sitesReducer,
})
