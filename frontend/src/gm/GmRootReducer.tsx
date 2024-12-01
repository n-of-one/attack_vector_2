import {Page, pageReducer} from "../common/menu/pageReducer";
import {SiteInfo, sitesReducer} from "../common/sites/SitesReducer";
import {combineReducers} from "redux";
import {editUserDataReducer, UserOverview, userOverviewReducer} from "../common/users/EditUserDataReducer";
import {currentUserReducer, GenericUserRootState, User} from "../common/users/CurrentUserReducer";
import {configReducer} from "../admin/config/ConfigReducer";
import {editScriptTypeReducer, ScriptType, scriptTypesReducer} from "./scripts/scriptType/ScriptTypeReducer";
import {ScriptAccess, scriptAccessReducer} from "./scripts/access/ScriptAccessReducer";


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
    editTypeId: String | null,
}
export const scriptManagementReducer = combineReducers({
    types: scriptTypesReducer,
    editTypeId: editScriptTypeReducer,
})


export interface GmRootState extends GenericUserRootState {
    currentPage: Page,
    users: Users,
    sites: Array<SiteInfo>,
    scriptsManagement: ScriptManagement
}
export const gmRootReducer = combineReducers({
    // from GenericUserRootState
    currentUser: currentUserReducer,
    config: configReducer,

    // from GmRootState
    currentPage: pageReducer,
    users: usersReducer,
    sites: sitesReducer,
    scriptsManagement: scriptManagementReducer,
})
