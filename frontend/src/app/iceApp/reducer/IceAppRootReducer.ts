import {AnyAction, combineReducers} from "redux";
import {pageReducer} from "../../../common/menu/pageReducer";
import {IceAppInfo, iceAppInfoReducer} from "./IceAppInfoReducer";
import {IceAppUi, iceAppUiReducer} from "./IceAppUiReducer";
import {noOpStringReducer} from "../../../common/reducer/NoOpReducer";

export interface IceAppRootState {
    currentPage: string,
    iceInfo: IceAppInfo
    ui: IceAppUi,
    layerId: string
}

const noOpReducer = (state: string = "", _action: AnyAction): string => {
    return state
}

export const iceAppRootReducer = combineReducers<IceAppRootState>(
    {
        currentPage: pageReducer,
        iceInfo: iceAppInfoReducer,
        ui: iceAppUiReducer,
        layerId: noOpStringReducer,
    }
)
