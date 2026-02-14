import {NAVIGATE_PAGE} from "../../common/menu/pageReducer";
import {CLOSE_SCRIPT_TYPE} from "../../common/script/type/ScriptTypeReducer";

const SERVER_SCRIPT_UI_FORCE_DELETE_ENABLED = "SERVER_SCRIPT_UI_FORCE_DELETE_ENABLED"

export interface ScriptUI {
    forceDeleteEnabled: boolean,
}

const defaultScriptUI: ScriptUI = {
    forceDeleteEnabled: false,
}

export const scriptUiReducer = (state: ScriptUI = defaultScriptUI, action: any): ScriptUI => {
    switch (action.type) {
        case SERVER_SCRIPT_UI_FORCE_DELETE_ENABLED:
            return {...state, forceDeleteEnabled: action.data.enabled}
        case CLOSE_SCRIPT_TYPE:
        case NAVIGATE_PAGE:
            return {...state, forceDeleteEnabled: false }
        default:
            return state
    }
}
