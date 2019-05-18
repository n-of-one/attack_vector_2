import {combineReducers} from 'redux'
import pageReducer from "../../common/menu/pageReducer";

import SiteReducer from "../../editor/reducer/SiteDataReducer";
import ScanReducer from "./reducer/ScanReducer";
import ThemeReducer from "../../common/reducer/ThemeReducer";
import UserReducer from "../../common/reducer/UserReducer";
import createTerminalReducer from "../../common/terminal/TerminalReducer";

const hackerReducer = combineReducers({
    currentPage: pageReducer,
    terminal: createTerminalReducer("main", {}),
    messageTerminal: createTerminalReducer("chat", {readonly: true, receiveBuffer: [{type:"text", data: "= chat online ="}]}),
    siteData: SiteReducer,
    scan: ScanReducer,
    theme: ThemeReducer,
    user: UserReducer
});

export default hackerReducer;