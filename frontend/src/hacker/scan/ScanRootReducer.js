import {combineReducers} from 'redux'

import ScanReducer from "./reducer/ScanReducer";
import createTerminalReducer from "../../common/terminal/TerminalReducer";
import SiteReducer from "./reducer/SiteReducer";

const scanRootReducer = combineReducers({
    messageTerminal: createTerminalReducer("chat", {readonly: true, receiveBuffer: [{type:"text", data: "= chat online ="}]}),
    site: SiteReducer,
    scan: ScanReducer,
});

export default scanRootReducer;