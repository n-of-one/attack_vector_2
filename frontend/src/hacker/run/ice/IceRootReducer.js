import {combineReducers} from "redux";
import PasswordIceReducer from "./password/PasswordIceReducer";
import createTerminalReducer from "../../../common/terminal/TerminalReducer";
import currentIceReducer from "./CurrentIceReducer";
import {ICE_DISPLAY_TERMINAL_ID, ICE_TERMINAL_ID} from "../../../common/terminal/ActiveTerminalIdReducer";



const iceRootReducer = combineReducers({

    currentIce: currentIceReducer,
    password: PasswordIceReducer,
    displayTerminal: createTerminalReducer(ICE_DISPLAY_TERMINAL_ID, {readOnly: true, receiveBuffer: []}),
    inputTerminal: createTerminalReducer(ICE_TERMINAL_ID, {renderOutput: false}),


});

export default iceRootReducer