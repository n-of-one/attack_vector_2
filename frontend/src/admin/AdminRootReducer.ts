import {currentUserReducer} from "../common/users/CurrentUserReducer";
import {configReducer} from "./config/ConfigReducer";
import {combineReducers} from "redux";
import {pageReducer} from "../common/menu/pageReducer";
import {GmRootState, scriptManagementReducer, usersReducer} from "../gm/GmRootReducer";
import {Task, tasksReducer} from "../gm/taskmonitor/TaskReducer";
import {sitesReducer} from "../common/sites/SitesReducer";
import {scriptStatusReducer} from "../common/script/ScriptStatusReducer";
import {incomeDateReducer} from "../gm/scripts/income/IncomeDateReducer";
import {creditTransactionReducer} from "../common/script/credits/CreditTransactionReducer";

export interface AdminRootState extends GmRootState {
    tasks: Task[]
}

export const adminRootReducer = combineReducers({
    // from GenericUserRootState
    currentUser: currentUserReducer,
    currentPage: pageReducer,
    config: configReducer,

    // from GmRootState
    users: usersReducer,
    sites: sitesReducer,
    scriptsManagement: scriptManagementReducer,
    scriptStatus: scriptStatusReducer,
    incomeDates: incomeDateReducer,
    creditTransactions: creditTransactionReducer,

    // from AdminRootState
    tasks: tasksReducer,
})

