import {createTerminalReducer, TerminalState} from "../../../../common/terminal/TerminalReducer";
import {ICE_DISPLAY_TERMINAL_ID} from "../../../../common/terminal/ActiveTerminalIdReducer";
import {combineReducers} from "redux";
import {WordSearchState, wordSearchStateReducer} from "./WordSearchStateReducer";
import {WordSearchPuzzle, wordSearchPuzzleReducer} from "./WordSearchPuzzleReducer";
import {UIState, wordSearchUiStateReducer} from "./WordSearchUiStateReducer";
import {Page, pageReducer} from "../../../../common/menu/pageReducer";
import {IceHackers, iceHackersReducer} from "../../common/IceHackersReducer";
import {WordSearchDisplayOptions, wordSearchDisplayOptionsReducer} from "./WordSearchDisplayOptionsReducer";


export interface WordSearchRootState {
    currentPage: Page,
    hackers: IceHackers,
    state: WordSearchState,
    puzzle: WordSearchPuzzle,
    uiState: UIState
    displayTerminal: TerminalState,
    displayOptions: WordSearchDisplayOptions,
}

const displayTerminalReducer = createTerminalReducer(ICE_DISPLAY_TERMINAL_ID, {readOnly: true, autoScroll: true});

export const wordSearchRootReducer = combineReducers<WordSearchRootState>(
    {
        currentPage: pageReducer,
        hackers: iceHackersReducer,
        state: wordSearchStateReducer,
        puzzle: wordSearchPuzzleReducer,
        uiState: wordSearchUiStateReducer,
        displayTerminal: displayTerminalReducer,
        displayOptions: wordSearchDisplayOptionsReducer,
    }
)

