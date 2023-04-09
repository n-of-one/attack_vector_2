import {HIDDEN, UNLOCKED} from "../../IceUiState";
import {AnyAction} from "redux";
import {NodeI} from "../../../editor/reducer/NodesReducer";

export const LETTERS_SELECTED = "LETTERS_SELECTED";
export const WORD_SEARCH_BEGIN = "WORD_SEARCH_BEGIN"


export interface WordSearchState {
    uiState: string,
    lettersCorrect: string[],
    lettersSelected: string[]
    wordIndex: number,
    hacked: boolean
}

const defaultState: WordSearchState = {
    uiState: HIDDEN,
    lettersCorrect: ["0:0", "0:1", "0:2", "0:3"],
    lettersSelected: [],
    wordIndex: 0,
    hacked: false
}


export const wordSearchStateReducer = (state: WordSearchState = defaultState, action: AnyAction): WordSearchState => {

    switch (action.type) {
        case LETTERS_SELECTED:
            return letterSelected(state, action)
        default:
            return state
    }
}

function letterSelected(state: WordSearchState, action: AnyAction) {
    const newVal = {...state, lettersSelected: action.payload}
    return newVal
}
