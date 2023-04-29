import {IceStrength} from "../../../common/model/IceStrength";
import {UNLOCKED} from "../../IceModel";

export const SERVER_ENTER_ICE_SLOW = "SERVER_ENTER_ICE_SLOW"
export const SLOW_ICE_BEGIN = "SLOW_ICE_BEGIN"
export const SLOW_ICE_UPDATE_UNITS_HACKED = "SLOW_ICE_UPDATE_UNITS_HACKED"


export interface SlowIceState {
    strength: IceStrength,
    hacked: boolean,
    uiState: "HIDDEN" | "UNLOCKED",
    totalUnits: number,
    unitsHacked: number
}

const defaultState: SlowIceState = {
    strength: IceStrength.AVERAGE,
    hacked: false,
    uiState: "HIDDEN",
    totalUnits: 1000,
    unitsHacked: 0
}

export const slowIceStateReducer = (state: SlowIceState = defaultState, action: any): SlowIceState => {

        switch (action.type) {
            case SERVER_ENTER_ICE_SLOW:
                return enter(state, action)
            case SLOW_ICE_BEGIN:
                return { ...state, uiState: UNLOCKED }
            case SLOW_ICE_UPDATE_UNITS_HACKED:
                return { ...state, unitsHacked: action.data.unitsHacked }
            default:
                return state
        }
}

const enter =  (state: SlowIceState, action: any): SlowIceState => {
    return {
        strength: action.data.strength,
        hacked: action.data.hacked,
        uiState: "HIDDEN",
        totalUnits: action.data.totalUnits,
        unitsHacked: action.data.unitsHacked
    }
}