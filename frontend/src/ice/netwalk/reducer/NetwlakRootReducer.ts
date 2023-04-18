import {createTerminalReducer, TerminalState} from "../../../common/terminal/TerminalReducer";
import {ICE_DISPLAY_TERMINAL_ID} from "../../../common/terminal/ActiveTerminalIdReducer";
import {AnyAction, combineReducers} from "redux";
import {NetwalkState, netwalkStateReducer} from "./NetwalkStateReducer";


export interface NetwalkRootState {
    iceId: string,
    state: NetwalkState,
    displayTerminal: TerminalState,
}

const displayTerminalReducer = createTerminalReducer(ICE_DISPLAY_TERMINAL_ID, {readOnly: true, receiveBuffer: [], autoScroll: true});

const noOpReducer = (state: string = "", action: AnyAction): string => {
    return state
}

export const netwlakRootReducer = combineReducers<NetwalkRootState>(
    {
        iceId: noOpReducer,
        state: netwalkStateReducer,
        displayTerminal: displayTerminalReducer
    }
)

