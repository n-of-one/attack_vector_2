import {combineReducers} from "redux";
import PasswordIceReducer from "./password/PasswordIceReducer";
import createTerminalReducer from "../../../common/terminal/TerminalReducer";

const iceRootReducer = combineReducers({

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
    inputTerminal: createTerminalReducer("iceInput", {renderOutput: false}),


});

export default iceRootReducer