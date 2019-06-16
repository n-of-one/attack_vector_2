import {combineReducers} from 'redux'

import ScanReducer from "./reducer/ScanReducer";
import createTerminalReducer from "../../common/terminal/TerminalReducer";
import SiteReducer from "./reducer/SiteReducer";
import infoNodeIdReducer from "./reducer/InfoNodeIdReducer";

const runRootReducer = combineReducers({
    messageTerminal: createTerminalReducer("chat", {readonly: true, receiveBuffer: [{type:"text", data: "= chat online ="}]}),
    site: SiteReducer,
    scan: ScanReducer,
    infoNodeId: infoNodeIdReducer,
});

export default runRootReducer;