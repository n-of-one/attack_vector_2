import {createTerminalReducer, TerminalState} from "../../../common/terminal/TerminalReducer";
import {ICE_DISPLAY_TERMINAL_ID, ICE_INPUT_TERMINAL_ID} from "../../../common/terminal/ActiveTerminalIdReducer";
import {passwordIceReducer, PasswordIceI} from "./password/PasswordIceReducer";
import {combineReducers} from "redux";

const displayTerminalReducer = createTerminalReducer(ICE_DISPLAY_TERMINAL_ID, {readOnly: true, receiveBuffer: [], autoScroll: true});
const inputTerminalReducer = createTerminalReducer(ICE_INPUT_TERMINAL_ID, {renderOutput: false});

export interface SiteIce {
    password?: PasswordIceI,
    displayTerminal: TerminalState,
    inputTerminal: TerminalState
}

export const iceRootReducer = combineReducers<SiteIce>({
    password: passwordIceReducer,
    displayTerminal: displayTerminalReducer,
    inputTerminal: inputTerminalReducer,
})

