import {AnyAction} from "redux";
import {SERVER_ENTER_ICE_WORD_SEARCH, SERVER_ICE_WORD_SEARCH_UPDATED} from "./WordSearchPuzzleReducer";

export const LETTERS_SELECTED = "LETTERS_SELECTED";
export const WORD_SEARCH_BEGIN = "WORD_SEARCH_BEGIN"


export interface WordSearchState {
    lettersCorrect: string[],
    lettersSelected: string[]
    wordIndex: number,
    hacked: boolean
}

const defaultState: WordSearchState = {
    lettersCorrect: ["0:0", "0:1", "0:2", "0:3"],
    lettersSelected: [],
    wordIndex: 0,
    hacked: false
}


export const wordSearchStateReducer = (state: WordSearchState = defaultState, action: AnyAction): WordSearchState => {

    switch (action.type) {
        case SERVER_ENTER_ICE_WORD_SEARCH:
            return stateFromAction(action)
        case SERVER_ICE_WORD_SEARCH_UPDATED:
            return stateFromAction(action)
        case LETTERS_SELECTED:
            return letterSelected(state, action)
        default:
            return state
    }
}

function stateFromAction( action: AnyAction) {
    return {
        lettersCorrect: action.data.lettersCorrect,
        lettersSelected: [],
        wordIndex: action.data.wordIndex,
        hacked: action.data.hacked
    }
}
function letterSelected(state: WordSearchState, action: AnyAction) {
    const newVal = {...state, lettersSelected: action.payload}
    return newVal
}
