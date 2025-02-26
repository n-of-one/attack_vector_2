import {combineReducers} from 'redux'

import {Scan, scanReducer} from "./reducer/ScanReducer";
import {Site, siteReducer} from "./reducer/SiteReducer";
import {infoNodeIdReducer} from "./reducer/InfoNodeIdReducer";
import {timersReducer, TimerState} from "./timer/TimersReducer";

export interface RunState {
    site: Site,
    run: Scan,
    infoNodeId: string | null, // Info panel of this node is currently being shown
    timers: TimerState[]
}

export const runRootReducer =
    combineReducers<RunState>({
        site: siteReducer,
        run: scanReducer,
        infoNodeId: infoNodeIdReducer,
        timers: timersReducer
    })
