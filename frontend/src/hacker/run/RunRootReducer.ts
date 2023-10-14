import {combineReducers} from 'redux'

import {Scan, scanReducer} from "./reducer/ScanReducer";
import {createTerminalReducer, TerminalLineType, TerminalState} from "../../common/terminal/TerminalReducer";
import {Site, siteReducer} from "./reducer/SiteReducer";
import {infoNodeIdReducer} from "./reducer/InfoNodeIdReducer";
import {countdownReducer, CountdownTimerState} from "./coundown/CountdownReducer";
import {CHAT_TERMINAL_ID} from "../../common/terminal/ActiveTerminalIdReducer";

export interface RunState {
    messageTerminal: TerminalState
    site: Site,
    run: Scan,
    infoNodeId: string | null, // Info panel of this node is currently being shown
    timers: CountdownTimerState[]
}

const chatTerminalReducer = createTerminalReducer(CHAT_TERMINAL_ID, {readOnly: true, receiveBuffer: [{type: TerminalLineType.TEXT, data: "= chat offline ="}]})

export const runRootReducer =
    combineReducers<RunState>({
        messageTerminal: chatTerminalReducer,
        site: siteReducer,
        run: scanReducer,
        infoNodeId: infoNodeIdReducer,
        timers: countdownReducer
    })
