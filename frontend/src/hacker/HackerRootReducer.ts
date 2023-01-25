import {combineReducers} from 'redux'
import {pageReducer} from "../common/menu/pageReducer";
import {homeRootReducer, HomeState} from "./home/HomeRootReducer";
import {mailRootReducer, MailRootState} from "./mail/MailRootReducer";
import {runRootReducer, RunState} from "./run/RunRootReducer";
import {createTerminalReducer, TerminalState} from "../common/terminal/TerminalReducer";
import {themeReducer} from "../common/reducer/ThemeReducer";
import {userIdReducer, UserIdState} from "../common/reducer/UserIdReducer";
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
    userId: UserIdState
}

const mainTerminalReducer = createTerminalReducer(MAIN_TERMINAL_ID, {autoScroll: true})

export const hackerRootReducer = combineReducers({
    currentPage: pageReducer,
    run: runRootReducer,
    home: homeRootReducer,
    mail: mailRootReducer,
    hackers: hackersReducer,
    terminal: mainTerminalReducer,
    activeTerminalId: activeTerminalIdReducer,
    theme: themeReducer,
    userId: userIdReducer
})
