import {pageReducerX} from "../common/menu/pageReducerX";
import {GmSite, gmSitesReducer} from "./GmSitesReducer";
import {combineReducers} from "redux";
import {EditUser, editUserReducer, User, userOverviewReducer} from "./users/UsersReducer";


const usersReducer = combineReducers({
    overview: userOverviewReducer,
    edit: editUserReducer,
})

export interface Users {
    overview: Array<User>,
    edit: EditUser
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
