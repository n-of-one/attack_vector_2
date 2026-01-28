import {AnyAction} from "redux";
import {SERVER_WORD_SEARCH_ENTER} from "./WordSearchPuzzleReducer";
import {ServerEnterIceWordSearch} from "../WordSearchServerActionProcessor";

export const LETTERS_SELECTED = "LETTERS_SELECTED";
export const WORD_SEARCH_BEGIN = "WORD_SEARCH_BEGIN"
export const LETTER_CORRECT_HIGHLIGHT = "LETTER_CORRECT_HIGHLIGHT"
export const LETTER_CORRECT = "LETTER_CORRECT"

export enum LetterState {
    HINT, // this is part of a solution. It can be during DEV to get a hint of what the solutions are.
    SELECTED,
    CORRECT_HIGHLIGHT,
    CORRECT
}

export type LetterTypeMap = { [key: string]: LetterState }

export interface WordSearchState {
    letters: LetterTypeMap,
    selected: LetterTypeMap,
}

const defaultState: WordSearchState = {
    letters: {},
    selected: {},
}


export const wordSearchStateReducer = (state: WordSearchState = defaultState, action: AnyAction): WordSearchState => {

    switch (action.type) {
        case SERVER_WORD_SEARCH_ENTER:
            return stateFromServer(state, action as unknown as WordSearchStateFromServer)
        case LETTER_CORRECT_HIGHLIGHT:
            return letterUpdate(state, action.positions, LetterState.CORRECT_HIGHLIGHT)
        case LETTER_CORRECT:
            return letterUpdate(state, action.positions, LetterState.CORRECT)
        case LETTERS_SELECTED:
            return letterSelected(state, action.selected)
        default:
            return state
    }
}

interface WordSearchStateFromServer {
    data: ServerEnterIceWordSearch
}

function stateFromServer(state: WordSearchState, action: WordSearchStateFromServer): WordSearchState {

    const letters: LetterTypeMap = {}

    action.data.solutions.forEach((solution: string[]) => {
        return solution.forEach((position: string) => {
            letters[position] = LetterState.HINT
        })
    })

    // correct-state letters trump hint-state letters
    action.data.correctPositions.forEach((position: string) => {
        letters[position] = LetterState.CORRECT
    })

    return {letters, selected: state.selected}
}


function letterUpdate(state: WordSearchState, positions: string[], letterState: LetterState) {
    const newLetters = {...state.letters}
    positions.forEach((position: string) => {
        newLetters[position] = letterState
    })

    return {...state, letters: newLetters}
}


function letterSelected(state: WordSearchState, selected: string[]) {

    const newSelected: LetterTypeMap = {}

    selected.forEach((position: string) => {
        newSelected[position] = LetterState.SELECTED
    })

    return {letters: state.letters, selected: newSelected}
}


