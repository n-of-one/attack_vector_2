import {combineReducers} from 'redux'
import pageReducer from "../common/reducer/pageReducer";

import terminalReducer from "../common/component/terminal/TerminalReducer"
import SiteReducer from "../editor/reducer/SiteReducer";
import ScanReducer from "./reducer/ScanReducer";

const hackerReducer = combineReducers({
    currentPage: pageReducer,
    terminal: terminalReducer,
    site: SiteReducer,
    scan: ScanReducer
});

export default hackerReducer;