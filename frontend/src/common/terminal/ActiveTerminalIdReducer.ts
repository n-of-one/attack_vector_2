import {SERVER_START_HACKING_ICE_PASSWORD} from "../../hacker/run/ice/password/PasswordIceActions";
import {FINISH_HACKING_ICE} from "../../hacker/run/model/HackActions";
import {AnyAction} from "redux";

export const MAIN_TERMINAL_ID = "main"
export const ICE_INPUT_TERMINAL_ID = "iceInput"
export const ICE_DISPLAY_TERMINAL_ID = "iceDisplay"

export type ActiveTerminalId = "main" |  "iceInput" | "iceDisplay"

const defaultState: ActiveTerminalId = MAIN_TERMINAL_ID;

export const activeTerminalIdReducer = (state: ActiveTerminalId = defaultState, action: AnyAction): ActiveTerminalId => {
    switch (action.type) {
        // case CHANGE_ACTIVE_TERMINAL:
        //     return action;
        case SERVER_START_HACKING_ICE_PASSWORD:
            return ICE_INPUT_TERMINAL_ID;
        case FINISH_HACKING_ICE:
            return MAIN_TERMINAL_ID;
        default:
            return state;
    }
}
