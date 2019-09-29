import {combineReducers} from 'redux'

import ScanReducer from "./reducer/ScanReducer";
import createTerminalReducer from "../../common/terminal/TerminalReducer";
import SiteReducer from "./reducer/SiteReducer";
import infoNodeIdReducer from "./reducer/InfoNodeIdReducer";
import iceRootReducer from "./ice/IceRootReducer";
import countdownReducer from "./coundown/CountdownReducer";


const runRootReducer = combineReducers({
    messageTerminal: createTerminalReducer("chat", {readOnly: true, receiveBuffer: [{type:"text", data: "= chat offline ="}]}),

    site: SiteReducer,
    scan: ScanReducer,
    infoNodeId: infoNodeIdReducer,
    ice: iceRootReducer,
    countdown: countdownReducer
});

export default runRootReducer;