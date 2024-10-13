import {createTerminalReducer, TerminalState} from "../../../../common/terminal/TerminalReducer";
import {ICE_DISPLAY_TERMINAL_ID} from "../../../../common/terminal/ActiveTerminalIdReducer";
import {combineReducers} from "redux";
import {NetwalkState, netwalkStateReducer} from "./NetwalkStateReducer";
import {pageReducer} from "../../../../common/menu/pageReducer";
import {IceHackers, iceHackersReducer} from "../../common/IceHackersReducer";


export interface NetwalkRootState {
    currentPage: string,
    hackers: IceHackers,
    state: NetwalkState,
    displayTerminal: TerminalState,
}

const displayTerminalReducer = createTerminalReducer(ICE_DISPLAY_TERMINAL_ID, {readOnly: true, autoScroll: true});

export const netwalkRootReducer = combineReducers<NetwalkRootState>(
    {
        currentPage: pageReducer,
        hackers: iceHackersReducer,
        state: netwalkStateReducer,
        displayTerminal: displayTerminalReducer,
    }
)

