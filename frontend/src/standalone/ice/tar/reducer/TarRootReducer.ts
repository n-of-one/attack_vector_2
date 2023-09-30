import {createTerminalReducer, TerminalState} from "../../../../common/terminal/TerminalReducer";
import {ICE_DISPLAY_TERMINAL_ID} from "../../../../common/terminal/ActiveTerminalIdReducer";
import {combineReducers} from "redux";
import {pageReducer} from "../../../../common/menu/pageReducer";
import {iceHackersReducer, IceHackers} from "../../common/IceHackersReducer";
import {TarState, tarStateReducer} from "./TarReducer";


export interface TarRootState {
    currentPage: string,
    hackers: IceHackers,
    iceState: TarState,
    displayTerminal: TerminalState,
}

const displayTerminalReducer = createTerminalReducer(ICE_DISPLAY_TERMINAL_ID, {readOnly: true, receiveBuffer: [], autoScroll: true});

export const tarRootReducer = combineReducers<TarRootState>(
    {
        currentPage: pageReducer,
        hackers: iceHackersReducer,
        iceState: tarStateReducer,
        displayTerminal: displayTerminalReducer
    }
)
