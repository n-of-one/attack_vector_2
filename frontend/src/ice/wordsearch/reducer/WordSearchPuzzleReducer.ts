import {AnyAction} from "redux";

export const WORD_SEARCH_BEGIN = "WORD_SEARCH_BEGIN";
export const SERVER_ENTER_ICE_WORD_SEARCH = "SERVER_ENTER_ICE_WORD_SEARCH";


export interface WordSearchPuzzle {
    layerId: string,
    strength: string,
    letters: string[][],
    words: string[]
}


const defaultState: WordSearchPuzzle = {
    layerId: "",
    strength: "",
    letters: [
        ['R', 'O', 'O', 'T', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'A', 'B', 'C', 'D', 'E', 'A', 'B', 'C', 'D', 'E', 'A', 'B', 'C', 'D', 'E', 'B', 'C', 'D', 'E', 'A', 'B', 'C', 'D', 'E',  'D', 'E'],
        ['A', 'B', 'C', 'D', 'E'],
        ['F', 'G', 'H', 'I', 'J'],
        ['K', 'L', 'M', 'N', 'O'],
        ['P', 'Q', 'R', 'S', 'T']
    ],
    words: ["ROOT", "ABCD", "EFGH", "IJKL", "MNOP", "QRST"]
};

export const wordSearchPuzzleReducer = (state: WordSearchPuzzle = defaultState, action: AnyAction): WordSearchPuzzle => {

    switch (action.type) {
        case SERVER_ENTER_ICE_WORD_SEARCH:
            return {...action.data};
        default:
            return state;
    }
};
