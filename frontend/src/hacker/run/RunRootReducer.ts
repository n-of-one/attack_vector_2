import {combineReducers} from 'redux'

import {Scan, scanReducer} from "./reducer/ScanReducer";
import {createTerminalReducer, TerminalState} from "../../common/terminal/TerminalReducer";
import {siteReducer, Site} from "./reducer/SiteReducer";
import {infoNodeIdReducer} from "./reducer/InfoNodeIdReducer";
import {iceRootReducer, SiteIce} from "./ice/IceRootReducer";
import {countdownReducer, CountDownState} from "./coundown/CountdownReducer";

export interface RunState {
    messageTerminal: TerminalState
    site: Site,
    scan: Scan,
    infoNodeId: string | null, // Info panel of this node is currently being shown
    ice: SiteIce
    countdown: CountDownState
}

const chatTerminalReducer = createTerminalReducer("chat", {readOnly: true, receiveBuffer: [{type: "text", data: "= chat offline ="}]})

export const runRootReducer =
    combineReducers<RunState>({
        messageTerminal: chatTerminalReducer,
        site: siteReducer,
        scan: scanReducer,
        infoNodeId: infoNodeIdReducer,
        ice: iceRootReducer,
        countdown: countdownReducer
    })
