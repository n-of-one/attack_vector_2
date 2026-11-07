import {AnyAction} from "redux";

export const WORD_SEARCH_DISPLAY_OPTIONS = "WORD_SEARCH_DISPLAY_OPTIONS"

export type WordSearchLetterColor = "DEFAULT" | "LIGHT_BLUE" | "AMBER"
export type WordSearchFontSize = 16 | 19 | 22
export type WordSearchGridLines = "DEFAULT" | "OFF" | "LIGHT"
export type WordSearchNoise = "ON" | "OFF"

export interface WordSearchDisplayOptions {
    color: WordSearchLetterColor,
    fontSize: WordSearchFontSize,
    gridLines: WordSearchGridLines,
    noise: WordSearchNoise,
}

const defaultState: WordSearchDisplayOptions = {
    color: "DEFAULT",
    fontSize: 16,
    gridLines: "DEFAULT",
    noise: "ON",
}

export const wordSearchDisplayOptionsReducer = (state: WordSearchDisplayOptions = defaultState, action: AnyAction): WordSearchDisplayOptions => {
    switch (action.type) {
        case WORD_SEARCH_DISPLAY_OPTIONS:
            return {...state, ...action.options}
        default:
            return state
    }
}
