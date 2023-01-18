import {combineReducers} from 'redux'
import {pageReducer} from "../common/menu/pageReducer";
import homeRootReducer from "./home/HomeRootReducer";
import mailRootReducer from "./mail/MailRootReducer";
import runRootReducer from "./run/RunRootReducer";
import createTerminalReducer from "../common/terminal/TerminalReducer";
import themeReducer from "../common/reducer/ThemeReducer";
import userIdReducer from "../common/reducer/UserIdReducer";
import hackersReducer from "./run/reducer/HackersReducer";
import {activeTerminalIdReducer, MAIN_TERMINAL_ID} from "../common/terminal/ActiveTerminalIdReducer";

const hackerRootReducer = combineReducers({
    currentPage: pageReducer,
    run: runRootReducer,
    home: homeRootReducer,
    mail: mailRootReducer,
    hackers: hackersReducer,
    terminal: createTerminalReducer(MAIN_TERMINAL_ID, {autoScroll: true}),
    activeTerminalId: activeTerminalIdReducer,
    theme: themeReducer,
    userId: userIdReducer

});

export default hackerRootReducer;