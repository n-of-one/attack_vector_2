import {NAVIGATE_PAGE} from "../../common/menu/pageReducer";
import {CLOSE_SCRIPT_TYPE} from "../../common/script/type/ScriptTypeReducer";

const SERVER_SCRIPT_UI_FORCE_DELETE_ENABLED = "SERVER_SCRIPT_UI_FORCE_DELETE_ENABLED"
export const SELECT_USER_TO_COPY_FROM = "SELECT_USER_TO_COPY_FROM"

export interface ScriptUI {
    forceDeleteEnabled: boolean,
    userIdToCopyFrom?: string,
}

const defaultScriptUI: ScriptUI = {
    forceDeleteEnabled: false,
    userIdToCopyFrom: undefined,
}

export const scriptUiReducer = (state: ScriptUI = defaultScriptUI, action: any): ScriptUI => {
    switch (action.type) {
        case SERVER_SCRIPT_UI_FORCE_DELETE_ENABLED:
            return {...state, forceDeleteEnabled: action.data.enabled}
        case CLOSE_SCRIPT_TYPE:
        case NAVIGATE_PAGE:
            return {...state, forceDeleteEnabled: false, userIdToCopyFrom: undefined }
        case SELECT_USER_TO_COPY_FROM:
                return {...state, userIdToCopyFrom: action.userId}
        default:
            return state
    }
}
