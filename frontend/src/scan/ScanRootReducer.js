import {combineReducers} from 'redux'
import pageReducer from "../common/reducer/pageReducer";

import terminalReducer from "../common/terminal/TerminalReducer"
import SiteReducer from "../editor/reducer/SiteDataReducer";
import ScanReducer from "./reducer/ScanReducer";
import ThemeReducer from "../common/reducer/ThemeReducer";
import UserReducer from "../common/reducer/UserReducer";

const hackerReducer = combineReducers({
    currentPage: pageReducer,
    terminal: terminalReducer,
    siteData: SiteReducer,
    scan: ScanReducer,
    theme: ThemeReducer,
    user: UserReducer
});

export default hackerReducer;