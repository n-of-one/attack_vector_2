import {CHANGE_ACTIVE_TERMINAL} from "./TerminalActions";
import {SERVER_START_HACKING_ICE_PASSWORD} from "../../hacker/run/ice/password/PasswordIceActions";

export const MAIN_TERMINAL_ID = "main";
export const ICE_TERMINAL_ID = "ice";
export const ICE_DISPLAY_TERMINAL_ID = "iceDisplay";

const defaultState = MAIN_TERMINAL_ID;

export const activeTerminalIdReducer = (state = defaultState, action) => {
    switch (action.type) {
        case CHANGE_ACTIVE_TERMINAL:
            return action;
        case SERVER_START_HACKING_ICE_PASSWORD:
            return ICE_TERMINAL_ID;
        default:
            return state;
    }
};


