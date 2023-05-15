import {createTerminalReducer, TerminalState} from "../../../common/terminal/TerminalReducer";
import {ICE_DISPLAY_TERMINAL_ID} from "../../../common/terminal/ActiveTerminalIdReducer";
import {AnyAction, combineReducers} from "redux";
import {NetwalkState, netwalkStateReducer} from "./NetwalkStateReducer";
import {pageReducer} from "../../../common/menu/pageReducer";
import {iceHackersReducer, IceHackers} from "../../common/IceHackersReducer";


export interface NetwalkRootState {
    iceId: string,
    currentPage: string,
    hackers: IceHackers,
    state: NetwalkState,
    displayTerminal: TerminalState,
}

const displayTerminalReducer = createTerminalReducer(ICE_DISPLAY_TERMINAL_ID, {readOnly: true, receiveBuffer: [], autoScroll: true});

const noOpReducer = (state: string = "", action: AnyAction): string => {
    return state
}

export const netwalkRootReducer = combineReducers<NetwalkRootState>(
    {
        iceId: noOpReducer,
        currentPage: pageReducer,
        hackers: iceHackersReducer,
        state: netwalkStateReducer,
        displayTerminal: displayTerminalReducer
    }
)

