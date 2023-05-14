import {pageReducer} from "../common/menu/pageReducer";
import {GmSite, gmSitesReducer} from "./sites/GmSitesReducer";
import {combineReducers} from "redux";
import {editUserReducer, User, userOverviewReducer} from "./users/UsersReducer";


export const usersReducer = combineReducers({
    overview: userOverviewReducer,
    edit: editUserReducer,
})

export interface Users {
    overview: Array<User>,
    edit: User | null
}

export interface GmState {
    currentPage: string,
    users: Users,
    sites: Array<GmSite>
}



export const gmRootReducer = combineReducers({
    currentPage: pageReducer,
    users: usersReducer,
    sites: gmSitesReducer,
})
