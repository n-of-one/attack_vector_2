import {combineReducers} from 'redux'
import pageReducer from "../common/menu/pageReducer";
import homeRootReducer from "./home/HomeRootReducer";
import mailRootReducer from "./mail/MailRootReducer";
import scanRootReducer from "./scan/ScanRootReducer";
import createTerminalReducer from "../common/terminal/TerminalReducer";
import themeReducer from "../common/reducer/ThemeReducer";
import userIdReducer from "../common/reducer/UserIdReducer";
import hackersReducer from "./scan/reducer/HackersReducer";

const hackerRootReducer = combineReducers({
    currentPage: pageReducer,
    scan: scanRootReducer,
    home: homeRootReducer,
    mail: mailRootReducer,
    hackers: hackersReducer,
    terminal: createTerminalReducer("main", {}),
    theme: themeReducer,
    userId: userIdReducer

});

export default hackerRootReducer;