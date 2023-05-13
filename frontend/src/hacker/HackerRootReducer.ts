import {combineReducers} from 'redux'
import {pageReducerX} from "../common/menu/pageReducerX";
import {homeRootReducer, HomeState} from "./home/HomeRootReducer";
import {mailRootReducer, MailRootState} from "./mail/MailRootReducer";
import {runRootReducer, RunState} from "./run/RunRootReducer";
import {createTerminalReducer, TerminalState} from "../common/terminal/TerminalReducer";
import {themeReducer} from "../common/reducer/ThemeReducer";
import {userIdReducer} from "../common/reducer/UserIdReducer";
import {HackerPresence, hackersReducer} from "./run/reducer/HackersReducer";
import {ActiveTerminalId, activeTerminalIdReducer, MAIN_TERMINAL_ID} from "../common/terminal/ActiveTerminalIdReducer";

export interface HackerState {
    currentPage: string,
    run: RunState,
    home: HomeState,
    mail: MailRootState,
    hackers: HackerPresence[]
    terminal: TerminalState,
    activeTerminalId:ActiveTerminalId,
    theme: string,
    userId: string
}

const mainTerminalReducer = createTerminalReducer(MAIN_TERMINAL_ID, {autoScroll: true, blockedWhileRendering: true})

export const hackerRootReducer = combineReducers({
    currentPage: pageReducerX,
    run: runRootReducer,
    home: homeRootReducer,
    mail: mailRootReducer,
    hackers: hackersReducer,
    terminal: mainTerminalReducer,
    activeTerminalId: activeTerminalIdReducer,
    theme: themeReducer,
    userId: userIdReducer
})
