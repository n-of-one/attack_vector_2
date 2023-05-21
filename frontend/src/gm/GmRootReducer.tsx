import {pageReducer} from "../common/menu/pageReducer";
import {GmSite, gmSitesReducer} from "./sites/GmSitesReducer";
import {combineReducers} from "redux";
import {editUserReducer, userOverviewReducer} from "../common/users/UsersReducer";
import {currentUserReducer, GenericUserRootState, User} from "../common/users/UserReducer";


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
    sites: Array<GmSite>,
    currentUser: User | null,
}



export const gmRootReducer = combineReducers({
    currentPage: pageReducer,
    users: usersReducer,
    sites: gmSitesReducer,
    currentUser: currentUserReducer,
})
