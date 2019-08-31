import createTerminalReducer from "../../../common/terminal/TerminalReducer";
import {ICE_DISPLAY_TERMINAL_ID, ICE_INPUT_TERMINAL_ID} from "../../../common/terminal/ActiveTerminalIdReducer";
import PasswordIceReducer from "./password/PasswordIceReducer";
import CurrentIceReducer from "./CurrentIceReducer";

const displayTerminalReducer = createTerminalReducer(ICE_DISPLAY_TERMINAL_ID, {readOnly: true, receiveBuffer: [], autoScroll: true});
const inputTerminalReducer = createTerminalReducer(ICE_INPUT_TERMINAL_ID, {renderOutput: false});

const iceRootReducer = (state, action) => {
    const newState = {};
    if (!state) {
        state = {};
    }

    newState.currentIce = CurrentIceReducer(state.currentIce, action);
    newState.password = PasswordIceReducer(state.password, action, newState.currentIce);
    newState.displayTerminal = displayTerminalReducer(state.displayTerminal, action);
    newState.inputTerminal =  inputTerminalReducer(state.inputTerminal, action);
    return newState;
};

export default iceRootReducer