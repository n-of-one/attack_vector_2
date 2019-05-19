import {combineReducers} from 'redux'
import pageReducer from "../common/menu/pageReducer";
import homeRootReducer from "./home/HomeRootReducer";
import mailRootReducer from "./mail/MailRootReducer";
import scanRootReducer from "./scan/ScanRootReducer";
import ThemeReducer from "../common/reducer/ThemeReducer";
import UserReducer from "../common/reducer/UserReducer";
import createTerminalReducer from "../common/terminal/TerminalReducer";

const hackerRootReducer = combineReducers({
    currentPage: pageReducer,
    scan: scanRootReducer,
    home: homeRootReducer,
    mail: mailRootReducer,

    terminal: createTerminalReducer("main", {}),
    theme: ThemeReducer,
    user: UserReducer

});

export default hackerRootReducer;