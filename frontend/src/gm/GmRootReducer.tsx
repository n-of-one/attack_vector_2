import {pageReducer} from "../common/menu/pageReducer";
import {SiteInfo, sitesReducer} from "../common/sites/SitesReducer";
import {combineReducers} from "redux";
import {editUserDataReducer, UserOverview, userOverviewReducer} from "../common/users/EditUserDataReducer";
import {currentUserReducer, GenericUserRootState, User} from "../common/users/CurrentUserReducer";
import {configReducer} from "../admin/config/ConfigReducer";
import {editScriptTypeReducer, ScriptType, scriptTypesReducer} from "../common/script/type/ScriptTypeReducer";
import {ScriptAccess, scriptAccessReducer} from "../common/script/access/ScriptAccessReducer";
import {ScriptStatistics, scriptStatisticsReducer} from "./scripts/ScriptStatisticsReducer";
import {ScriptStatus, scriptStatusReducer} from "../common/script/ScriptStatusReducer";
import {IncomeDate, incomeDateReducer} from "./scripts/income/IncomeDateReducer";
import {CreditTransaction, creditTransactionReducer} from "../common/script/credits/CreditTransactionReducer";


export interface EditUser {
    userData: User | null,
    scriptAccess: ScriptAccess[],
}

export const editUserReducer = combineReducers({
    userData: editUserDataReducer,
    scriptAccess: scriptAccessReducer,
})


export interface Users {
    overview: UserOverview[],
    edit: EditUser
}
export const usersReducer = combineReducers({
    overview: userOverviewReducer,
    edit: editUserReducer,
})


export interface ScriptManagement {
    types: ScriptType[],
    editTypeId: string | null,
    statistics: ScriptStatistics[],
}
export const scriptManagementReducer = combineReducers({
    types: scriptTypesReducer,
    editTypeId: editScriptTypeReducer,
    statistics: scriptStatisticsReducer,
})


export interface GmRootState extends GenericUserRootState {
    users: Users,
    sites: Array<SiteInfo>,
    scriptsManagement: ScriptManagement,
    scriptStatus: ScriptStatus | null,
    incomeDates: IncomeDate[],
    creditTransactions: CreditTransaction[],
}

export const gmRootReducer = combineReducers({
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
})
