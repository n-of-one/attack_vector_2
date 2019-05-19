import {combineReducers} from 'redux'

import SiteReducer from "../../editor/reducer/SiteDataReducer";
import ScanReducer from "./reducer/ScanReducer";
import createTerminalReducer from "../../common/terminal/TerminalReducer";

const scanRootReducer = combineReducers({
    messageTerminal: createTerminalReducer("chat", {readonly: true, receiveBuffer: [{type:"text", data: "= chat online ="}]}),
    siteData: SiteReducer,
    scan: ScanReducer,
});

export default scanRootReducer;