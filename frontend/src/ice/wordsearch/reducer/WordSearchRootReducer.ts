import {createTerminalReducer, TerminalState} from "../../../common/terminal/TerminalReducer";
import {ICE_DISPLAY_TERMINAL_ID} from "../../../common/terminal/ActiveTerminalIdReducer";
import {AnyAction, combineReducers} from "redux";
import {WordSearchState, wordSearchStateReducer} from "./WordSearchStateReducer";
import {WordSearchPuzzle, wordSearchPuzzleReducer} from "./WordSearchPuzzleReducer";
import {UIState, wordSearchUiStateReducer} from "./WordSearchUiStateReducer";
import {pageReducer} from "../../../common/menu/pageReducer";


export interface WordSearchRootState {
    iceId: string,
    currentPage: string,
    state: WordSearchState,
    puzzle: WordSearchPuzzle,
    uiState: UIState
    displayTerminal: TerminalState,
}

const displayTerminalReducer = createTerminalReducer(ICE_DISPLAY_TERMINAL_ID, {readOnly: true, receiveBuffer: [], autoScroll: true});

const noOpReducer = (state: string = "", action: AnyAction): string => {
    return state
}

export const wordSearchRootReducer = combineReducers<WordSearchRootState>(
    {
        iceId: noOpReducer,
        currentPage: pageReducer,
        state: wordSearchStateReducer,
        puzzle: wordSearchPuzzleReducer,
        uiState: wordSearchUiStateReducer,
        displayTerminal: displayTerminalReducer
    }
)

