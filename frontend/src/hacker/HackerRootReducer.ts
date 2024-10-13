import {combineReducers} from 'redux'
import {pageReducer} from "../common/menu/pageReducer";
import {runRootReducer, RunState} from "./run/RunRootReducer";
import {createTerminalReducer, TerminalState} from "../common/terminal/TerminalReducer";
import {themeReducer} from "../common/reducer/ThemeReducer";
import {HackerPresence, hackersReducer} from "./run/reducer/HackersReducer";
import {ActiveTerminalId, activeTerminalIdReducer, MAIN_TERMINAL_ID} from "../common/terminal/ActiveTerminalIdReducer";
import {Users, usersReducer} from "../gm/GmRootReducer";
import {currentUserReducer, GenericUserRootState, HackerSkill} from "../common/users/UserReducer";
import {hackerRunsReducer, RunInfo} from "./home/HackerRunsReducer";
import {SiteInfo, sitesReducer} from "../common/sites/SitesReducer";
import {configReducer} from "../admin/config/ConfigReducer";
import {skillsReducer} from "./SkillsReducer";

export interface HackerRootState extends GenericUserRootState {
    currentPage: string,
    run: RunState,
    runs: RunInfo[],
    sites: SiteInfo[],
    hackers: HackerPresence[]
    terminal: TerminalState,
    activeTerminalId:ActiveTerminalId,
    theme: string,
    users: Users,
    skills: HackerSkill[] | null
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
    hackers: hackersReducer,
    terminal: mainTerminalReducer,
    activeTerminalId: activeTerminalIdReducer,
    theme: themeReducer,
    users: usersReducer,
    skills: skillsReducer,
})
