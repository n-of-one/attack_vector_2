import {AnyAction} from "redux";
import {ServerEnterIceWordSearch} from "../WordSearchServerActionProcessor";
import {IceStrength} from "../../../common/model/IceStrength";

export const SERVER_ENTER_ICE_WORD_SEARCH = "SERVER_ENTER_ICE_WORD_SEARCH"



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
        case SERVER_ENTER_ICE_WORD_SEARCH: return enter(action.data)
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
