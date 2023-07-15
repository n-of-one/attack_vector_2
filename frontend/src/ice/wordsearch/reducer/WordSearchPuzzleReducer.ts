import {AnyAction} from "redux";
import {ServerEnterIceWordSearch} from "../WordSearchServerActionProcessor";
import {IceStrength} from "../../../common/model/IceStrength";

export const SERVER_WORD_SEARCH_ENTER = "SERVER_WORD_SEARCH_ENTER"



export interface WordSearchPuzzle {
    layerId: string,
    strength: IceStrength,
    letterGrid: string[][],
    words: string[],
    solutions: string[][],
}


const defaultState: WordSearchPuzzle = {
    layerId: "",
    strength: IceStrength.AVERAGE,
    letterGrid: [[]],
    words: [],
    solutions: [[]]
};

export const wordSearchPuzzleReducer = (state: WordSearchPuzzle = defaultState, action: AnyAction): WordSearchPuzzle => {

    switch (action.type) {
        case SERVER_WORD_SEARCH_ENTER: return enter(action.data)
        default: return state
    }
}

const enter = (action: ServerEnterIceWordSearch) => {
    return {
        layerId: action.layerId,
        strength: action.strength,
        letterGrid: action.letterGrid,
        words: action.words,
        solutions: action.solutions,
    }
}
