import {Page, pageReducer} from "../common/menu/pageReducer";
import {SiteInfo, sitesReducer} from "../common/sites/SitesReducer";
import {combineReducers} from "redux";
import {editUserReducer, userOverviewReducer} from "../common/users/EditUserReducer";
import {currentUserReducer, GenericUserRootState, User} from "../common/users/CurrentUserReducer";
import {configReducer} from "../admin/config/ConfigReducer";
import {editScriptTypeReducer, ScriptType, scriptTypesReducer} from "./scripts/scriptType/ScriptTypeReducer";


export interface Users {
    overview: Array<User>,
    edit: User | null
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
