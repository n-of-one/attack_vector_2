import {AnyAction} from "redux";

export const WORD_SEARCH_BEGIN = "WORD_SEARCH_BEGIN"
export const SERVER_ENTER_ICE_WORD_SEARCH = "SERVER_ENTER_ICE_WORD_SEARCH"
export const SERVER_ICE_WORD_SEARCH_UPDATED = "SERVER_ICE_WORD_SEARCH_UPDATED"


export interface WordSearchPuzzle {
    layerId: string,
    strength: string,
    letters: string[][],
    words: string[],
    solutions: string[][],
}


const defaultState: WordSearchPuzzle = {
    layerId: "",
    strength: "",
    letters: [[]],
    words: [],
    solutions: [[]]
};

export const wordSearchPuzzleReducer = (state: WordSearchPuzzle = defaultState, action: AnyAction): WordSearchPuzzle => {

    switch (action.type) {
        case SERVER_ENTER_ICE_WORD_SEARCH:
            return {
                layerId: action.data.layerId,
                strength: action.data.strength,
                letters: action.data.letters,
                words: action.data.words,
                solutions: action.data.solutions,
            }
        default: return state
    }
}
