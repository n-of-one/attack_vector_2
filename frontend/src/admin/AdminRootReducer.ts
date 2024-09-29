import {currentUserReducer, User} from "../common/users/UserReducer";
import { configReducer, ConfigState} from "./config/ConfigReducer";
import {combineReducers} from "redux";
import {pageReducer} from "../common/menu/pageReducer";

export interface AdminRootState {
    currentPage: string,
    currentUser: User | null,
    config: ConfigState
    // tasks: Array<Task>

}

export const adminRootReducer = combineReducers({
    currentPage: pageReducer,
    currentUser: currentUserReducer,
    config: configReducer,
    // task: tasksReducer,
})

