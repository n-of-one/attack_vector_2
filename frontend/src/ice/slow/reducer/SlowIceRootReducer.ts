import {createTerminalReducer, TerminalState} from "../../../common/terminal/TerminalReducer";
import {ICE_DISPLAY_TERMINAL_ID} from "../../../common/terminal/ActiveTerminalIdReducer";
import {AnyAction, combineReducers} from "redux";
import {SlowIceState, slowIceStateReducer} from "./SlowIceReducer";


export interface SlowIceRootState {
    iceId: string,
    iceState: SlowIceState,
    displayTerminal: TerminalState,
}

const displayTerminalReducer = createTerminalReducer(ICE_DISPLAY_TERMINAL_ID, {readOnly: true, receiveBuffer: [], autoScroll: true});

const noOpReducer = (state: string = "", action: AnyAction): string => {
    return state
}

export const slowIceRootReducer = combineReducers<SlowIceRootState>(
    {
        iceId: noOpReducer,
        iceState: slowIceStateReducer,
        displayTerminal: displayTerminalReducer
    }
)

