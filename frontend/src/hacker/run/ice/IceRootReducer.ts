import {createTerminalReducer, TerminalState} from "../../../common/terminal/TerminalReducer";
import {ICE_DISPLAY_TERMINAL_ID, ICE_INPUT_TERMINAL_ID} from "../../../common/terminal/ActiveTerminalIdReducer";
import {CurrentIce, currentIceDefault, currentIceReducer} from "./CurrentIceReducer";
import {passwordIceReducer, PasswordIceI} from "./password/PasswordIceReducer";
import {TangleIceState, tangleIceReducer} from "./tangle/TangleIceReducer";
import {AnyAction} from "redux";

const displayTerminalReducer = createTerminalReducer(ICE_DISPLAY_TERMINAL_ID, {readOnly: true, receiveBuffer: [], autoScroll: true});
const inputTerminalReducer = createTerminalReducer(ICE_INPUT_TERMINAL_ID, {renderOutput: false});

export interface SiteIce {
    currentIce: CurrentIce,
    password?: PasswordIceI,
    tangle?: TangleIceState,
    displayTerminal: TerminalState,
    inputTerminal: TerminalState
}

export const iceRootReducer = (state: SiteIce | undefined, action: AnyAction): SiteIce => {
    if (state === undefined) {
        return {
            currentIce: currentIceReducer(undefined, action),
            password: passwordIceReducer(undefined, action, currentIceDefault),
            tangle: tangleIceReducer(undefined, action, currentIceDefault),
            displayTerminal: displayTerminalReducer(undefined, action),
            inputTerminal: inputTerminalReducer(undefined, action),
        }
    }

    const currentIce = currentIceReducer(state.currentIce, action)
    return {
        currentIce: currentIceReducer(state.currentIce, action),
        password: passwordIceReducer(state.password, action, currentIce),
        tangle: tangleIceReducer(state.tangle, action, currentIce),
        displayTerminal: displayTerminalReducer(state.displayTerminal, action),
        inputTerminal: inputTerminalReducer(state.inputTerminal, action),
    }

}
