import {createTerminalReducer, TerminalState} from "../../common/terminal/TerminalReducer";
import {ICE_DISPLAY_TERMINAL_ID} from "../../common/terminal/ActiveTerminalIdReducer";
import {AnyAction, combineReducers} from "redux";
import {tangleIceReducer, TangleIceState} from "./TangleIceReducer";
import {pageReducer} from "../../common/menu/pageReducer";


export interface TangleRootState {
    iceId: string,
    currentPage: string,
    tangle: TangleIceState,
    displayTerminal: TerminalState,
}

const displayTerminalReducer = createTerminalReducer(ICE_DISPLAY_TERMINAL_ID, {readOnly: true, receiveBuffer: [], autoScroll: true});

const noOpReducer = (state: string = "", action: AnyAction): string => {
    return state
}

export const tangleRootReducer = combineReducers<TangleRootState>(
    {
        iceId: noOpReducer,
        currentPage: pageReducer,
        tangle: tangleIceReducer,
        displayTerminal: displayTerminalReducer
    }
)

