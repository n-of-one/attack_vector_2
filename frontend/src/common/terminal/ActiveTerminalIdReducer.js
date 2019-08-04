import {CHANGE_ACTIVE_TERMINAL} from "./TerminalActions";
import {SERVER_START_HACKING_ICE_PASSWORD} from "../../hacker/run/ice/password/PasswordIceActions";

const MAIN_TERMINAL_ID = "main";
const ICE_TERMINAL_ID = "ice";

const defaultState = MAIN_TERMINAL_ID;

const activeTerminalIdReducer = (state = defaultState, action) => {
    switch (action.type) {
        case CHANGE_ACTIVE_TERMINAL:
            return action;
        case SERVER_START_HACKING_ICE_PASSWORD:
            return ICE_TERMINAL_ID;
        default:
            return state;
    }
};


export {activeTerminalIdReducer, MAIN_TERMINAL_ID, ICE_TERMINAL_ID}
