import {createTerminalReducer, TerminalState} from "../../../../common/terminal/TerminalReducer";
import {ICE_DISPLAY_TERMINAL_ID} from "../../../../common/terminal/ActiveTerminalIdReducer";
import {combineReducers} from "redux";
import {WordSearchState, wordSearchStateReducer} from "./WordSearchStateReducer";
import {WordSearchPuzzle, wordSearchPuzzleReducer} from "./WordSearchPuzzleReducer";
import {UIState, wordSearchUiStateReducer} from "./WordSearchUiStateReducer";
import {pageReducer} from "../../../../common/menu/pageReducer";
import {iceHackersReducer, IceHackers} from "../../common/IceHackersReducer";


export interface WordSearchRootState {
    currentPage: string,
    hackers: IceHackers,
    state: WordSearchState,
    puzzle: WordSearchPuzzle,
    uiState: UIState
    displayTerminal: TerminalState,
}

const displayTerminalReducer = createTerminalReducer(ICE_DISPLAY_TERMINAL_ID, {readOnly: true, receiveBuffer: [], autoScroll: true});

export const wordSearchRootReducer = combineReducers<WordSearchRootState>(
    {
        currentPage: pageReducer,
        hackers: iceHackersReducer,
        state: wordSearchStateReducer,
        puzzle: wordSearchPuzzleReducer,
        uiState: wordSearchUiStateReducer,
        displayTerminal: displayTerminalReducer
    }
)

