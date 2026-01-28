import {combineReducers} from "redux";
import {Page, pageReducer} from "../../../../common/menu/pageReducer";
import {AuthAppInfo, authInfoReducer} from "./AuthInfoReducer";
import {AuthAppUi, authUiReducer} from "./AuthUiReducer";

export interface AuthAppRootState {
    currentPage: Page,
    info: AuthAppInfo
    ui: AuthAppUi,
    layerId: string,
}

export const authRootReducer = combineReducers<AuthAppRootState>(
    {
        currentPage: pageReducer,
        info: authInfoReducer,
        ui: authUiReducer,
        layerId: (state = "") => state,
    }
)
