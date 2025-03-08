import {AnyAction} from "redux";

const SERVER_SCRIPT_TYPES = "SERVER_SCRIPT_TYPES"
const SERVER_EDIT_SCRIPT_TYPE = "SERVER_EDIT_SCRIPT_TYPE"
export const EDIT_SCRIPT_TYPE = "EDIT_SCRIPT_TYPE"
export const CLOSE_SCRIPT_TYPE = "CLOSE_SCRIPT_TYPE"

export enum EffectType {
    DELAY_TRIPWIRE_COUNTDOWN = "DELAY_TRIPWIRE_COUNTDOWN",
    DECREASE_FUTURE_TIMERS = "DECREASE_FUTURE_TIMERS",
    HIDDEN_EFFECTS = "HIDDEN_EFFECTS",
    START_RESET_TIMER = "START_RESET_TIMER",
    SPEED_UP_RESET_TIMER = "SPEED_UP_RESET_TIMER",
    SCAN_ICE_NODE = "SCAN_ICE_NODE",
    SITE_STATS = "SITE_STATS",
    JUMP_TO_NODE = "JUMP_TO_NODE",
    JUMP_TO_HACKER = "JUMP_TO_HACKER",
    SWEEPER_UNBLOCK = "SWEEPER_UNBLOCK",
    WORD_SEARCH_NEXT_WORDS = "WORD_SEARCH_NEXT_WORDS",
    AUTO_HACK_SPECIFIC_ICE = "AUTO_HACK_SPECIFIC_ICE",
    AUTO_HACK_ANY_ICE = "AUTO_HACK_ANY_ICE",
}

export interface Effect {
    effectNumber: number,
    value: string | null,
    name: string,
    playerDescription: string,
    gmDescription: string,
    hidden: boolean,
    type: EffectType,
}

export interface ScriptType {
    id: string,
    name: string,
    size: number,
    defaultPrice?: number,
    effects: Effect[]
}


export const scriptTypesReducer = (state: ScriptType[] = [], action: AnyAction): ScriptType[] => {
    switch (action.type) {
        case SERVER_SCRIPT_TYPES:
            return enrichTypesFromServer(action.data)
        default:
            return state
    }
}

const enrichTypesFromServer = (types: ScriptType[]): ScriptType[] => {
    return types.map((type: ScriptType) => {
        return {
            ...type,
            effects: type.effects.map((effect: Effect, index: number) => {
                return {
                    ...effect,
                    effectNumber: (index + 1)
                }
            })
        }
    })
}

export const editScriptTypeReducer = (state: String | null = null, action: AnyAction): String | null => {
    switch (action.type) {
        case EDIT_SCRIPT_TYPE:
        case SERVER_EDIT_SCRIPT_TYPE:
            return action.data
        case CLOSE_SCRIPT_TYPE:
            return null
        default:
            return state
    }
}
