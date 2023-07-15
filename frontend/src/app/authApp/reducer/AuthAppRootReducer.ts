import {combineReducers} from "redux";
import {pageReducer} from "../../../common/menu/pageReducer";
import {AuthAppInfo, authAppInfoReducer} from "./AuthAppInfoReducer";
import {AuthAppUi, authAppUiReducer} from "./AuthAppUiReducer";

export interface AuthAppRootState {
    currentPage: string,
    info: AuthAppInfo
    ui: AuthAppUi,
}

export const authAppRootReducer = combineReducers<AuthAppRootState>(
    {
        currentPage: pageReducer,
        info: authAppInfoReducer,
        ui: authAppUiReducer,
    }
)
