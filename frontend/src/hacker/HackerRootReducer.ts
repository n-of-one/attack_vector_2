import {combineReducers} from 'redux'
import {pageReducer} from "../common/menu/pageReducer";
import {mailRootReducer, MailRootState} from "./mail/MailRootReducer";
import {runRootReducer, RunState} from "./run/RunRootReducer";
import {createTerminalReducer, TerminalState} from "../common/terminal/TerminalReducer";
import {themeReducer} from "../common/reducer/ThemeReducer";
import {HackerPresence, hackersReducer} from "./run/reducer/HackersReducer";
import {ActiveTerminalId, activeTerminalIdReducer, MAIN_TERMINAL_ID} from "../common/terminal/ActiveTerminalIdReducer";
import {Users, usersReducer} from "../gm/GmRootReducer";
import {currentUserReducer, GenericUserRootState, User} from "../common/users/UserReducer";
import {hackerRunsReducer, RunInfo} from "./home/HackerRunsReducer";
import {SiteInfo, sitesReducer} from "../common/sites/SitesReducer";

export interface HackerState extends GenericUserRootState {
    currentPage: string,
    run: RunState,
    runs: RunInfo[],
    sites: SiteInfo[],
    mail: MailRootState,
    hackers: HackerPresence[]
    terminal: TerminalState,
    activeTerminalId:ActiveTerminalId,
    theme: string,
    users: Users,
    currentUser: User | null,
}

const mainTerminalReducer = createTerminalReducer(MAIN_TERMINAL_ID, {autoScroll: true, blockedWhileRendering: true})

export const hackerRootReducer = combineReducers({
    currentPage: pageReducer,
    run: runRootReducer,
    runs: hackerRunsReducer,
    sites: sitesReducer,
    mail: mailRootReducer,
    hackers: hackersReducer,
    terminal: mainTerminalReducer,
    activeTerminalId: activeTerminalIdReducer,
    theme: themeReducer,
    users: usersReducer,
    currentUser: currentUserReducer,
})
