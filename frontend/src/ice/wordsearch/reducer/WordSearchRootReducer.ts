import {createTerminalReducer, TerminalState} from "../../../common/terminal/TerminalReducer";
import {ICE_DISPLAY_TERMINAL_ID} from "../../../common/terminal/ActiveTerminalIdReducer";
import {AnyAction, combineReducers} from "redux";
import {WordSearchState, wordSearchStateReducer} from "./WordSearchStateReducer";
import {WordSearchPuzzle, wordSearchPuzzleReducer} from "./WordSearchPuzzleReducer";


export interface WordSearchRootState {
    iceId: string,
    state: WordSearchState,
    puzzle: WordSearchPuzzle

    displayTerminal: TerminalState,
}

const displayTerminalReducer = createTerminalReducer(ICE_DISPLAY_TERMINAL_ID, {readOnly: true, receiveBuffer: [], autoScroll: true});

const noOpReducer = (state: string = "", action: AnyAction): string => {
    return state
}

export const wordSearchRootReducer = combineReducers<WordSearchRootState>(
    {
        iceId: noOpReducer,
        state: wordSearchStateReducer,
        puzzle: wordSearchPuzzleReducer,
        displayTerminal: displayTerminalReducer
    }
)

