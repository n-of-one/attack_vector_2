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
    SCAN_ICE_NODE = "SCAN_ICE_NODE"
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
    ram: number,
    defaultPrice?: number,
    effects: Effect[]
}


export const scriptTypesReducer = (state: ScriptType[] = [], action: AnyAction): ScriptType[] => {
    switch (action.type) {
        case SERVER_SCRIPT_TYPES:
            return enrichtTypesFromServer(action.data)
        default:
            return state
    }
}

const enrichtTypesFromServer = (types: ScriptType[]): ScriptType[] => {
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
