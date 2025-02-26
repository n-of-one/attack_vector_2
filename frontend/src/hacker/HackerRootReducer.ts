import {combineReducers} from 'redux'
import {Page, pageReducer} from "../common/menu/pageReducer";
import {runRootReducer, RunState} from "./run/RunRootReducer";
import {createTerminalReducer, TerminalState} from "../common/terminal/TerminalReducer";
import {themeReducer} from "../common/reducer/ThemeReducer";
import {HackerPresence, hackerPresencesReducer} from "./run/reducer/HackerPresencesReducer";
import {ActiveTerminalId, activeTerminalIdReducer, MAIN_TERMINAL_ID} from "../common/terminal/ActiveTerminalIdReducer";
import {Users, usersReducer} from "../gm/GmRootReducer";
import {currentUserReducer, GenericUserRootState} from "../common/users/CurrentUserReducer";
import {hackerRunsReducer, RunInfo} from "./home/HackerRunsReducer";
import {SiteInfo, sitesReducer} from "../common/sites/SitesReducer";
import {configReducer} from "../admin/config/ConfigReducer";
import {ScriptAccess, scriptAccessReducer} from "../gm/scripts/access/ScriptAccessReducer";
import {ScriptStatus, scriptStatusReducer} from "../common/script/ScriptStatusReducer";

export interface HackerRootState extends GenericUserRootState {
    currentPage: Page,
    run: RunState,
    runs: RunInfo[],
    sites: SiteInfo[],
    hackers: HackerPresence[]
    terminal: TerminalState,
    activeTerminalId:ActiveTerminalId,
    theme: string,
    users: Users,
    scriptAccess: ScriptAccess[],
    scriptStatus: ScriptStatus|null,
}

const mainTerminalReducer = createTerminalReducer(MAIN_TERMINAL_ID, {autoScroll: true, blockedWhileRendering: true})

export const hackerRootReducer = combineReducers({
    // from GenericUserRootState
    currentUser: currentUserReducer,
    config: configReducer,

    // from HackerRootState
    currentPage: pageReducer,
    run: runRootReducer,
    runs: hackerRunsReducer,
    sites: sitesReducer,
    hackers: hackerPresencesReducer,
    terminal: mainTerminalReducer,
    activeTerminalId: activeTerminalIdReducer,
    theme: themeReducer,
    users: usersReducer,
    scriptAccess: scriptAccessReducer,
    scriptStatus: scriptStatusReducer,
})
