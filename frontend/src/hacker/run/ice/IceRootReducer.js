import {combineReducers} from "redux";
import PasswordIceReducer from "./password/PasswordIceReducer";
import createTerminalReducer from "../../../common/terminal/TerminalReducer";
import currentIceReducer from "./CurrentIceReducer";
import {ICE_TERMINAL_ID} from "../../../common/terminal/ActiveTerminalIdReducer";

const iceRootReducer = combineReducers({

    currentIce: currentIceReducer,
    password: PasswordIceReducer,
    displayTerminal: createTerminalReducer("iceDisplay", {readOnly: true, receiveBuffer: [
            {type:"text", data: "↼ Connecting to ice, initiating attack."},
            {type:"text", data: "↼ Scanning for weaknesses."},
            {type:"text", data: "↼ ......................................................................................................................."},
            {type:"text", data: "↼ Found weak interface: static (non-rotating) password."},
            {type:"text", data: "↼ Attempting brute force..."},
            {type:"text", data: "↺ Detected incremental time-out."},
            {type:"text", data: "↺ Failed to sidestep incremental time-out."},
            {type:"text", data: "↼ Suggested attack vectors: retrieve password, informed password guessing."},
        ]}),
    inputTerminal: createTerminalReducer(ICE_TERMINAL_ID, {renderOutput: false}),


});

export default iceRootReducer