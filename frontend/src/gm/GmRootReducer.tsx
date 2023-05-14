import {pageReducerX} from "../common/menu/pageReducerX";
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
    currentPage: pageReducerX,
    users: usersReducer,
    sites: gmSitesReducer,
})
