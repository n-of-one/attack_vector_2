import {createTerminalReducer, TerminalState} from "../../../common/terminal/TerminalReducer";
import {ICE_DISPLAY_TERMINAL_ID} from "../../../common/terminal/ActiveTerminalIdReducer";
import {AnyAction, combineReducers} from "redux";
import {pageReducer} from "../../../common/menu/pageReducer";
import {iceHackersReducer, IceHackers} from "../../common/IceHackersReducer";
import {TarState, tarStateReducer} from "./TarReducer";


export interface TarRootState {
    iceId: string,
    currentPage: string,
    hackers: IceHackers,
    iceState: TarState,
    displayTerminal: TerminalState,
}

const displayTerminalReducer = createTerminalReducer(ICE_DISPLAY_TERMINAL_ID, {readOnly: true, receiveBuffer: [], autoScroll: true});

const noOpReducer = (state: string = "", action: AnyAction): string => {
    return state
}

export const tarRootReducer = combineReducers<TarRootState>(
    {
        iceId: noOpReducer,
        currentPage: pageReducer,
        hackers: iceHackersReducer,
        iceState: tarStateReducer,
        displayTerminal: displayTerminalReducer
    }
)

