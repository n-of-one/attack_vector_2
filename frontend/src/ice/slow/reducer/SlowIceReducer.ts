import {IceStrength} from "../../../common/model/IceStrength";
import {HIDDEN, UNLOCKED} from "../../IceModel";

export const SERVER_ENTER_ICE_SLOW = "SERVER_ENTER_ICE_SLOW"
export const SLOW_ICE_BEGIN = "SLOW_ICE_BEGIN"


export interface SlowIceState {
    strength: IceStrength,
    hacked: boolean,
    uiState: "HIDDEN" | "UNLOCKED",
}

const defaultState: SlowIceState = {
    strength: IceStrength.AVERAGE,
    hacked: false,
    uiState: HIDDEN,
}

export const slowIceStateReducer = (state: SlowIceState = defaultState, action: any): SlowIceState => {

        switch (action.type) {
            case SERVER_ENTER_ICE_SLOW:
                return enter(state, action)
            case SLOW_ICE_BEGIN:
                return { ...state, uiState: UNLOCKED }
            default:
                return state
        }
}

const enter =  (state: SlowIceState, action: any): SlowIceState => {
    return {
        strength: action.data.strength,
        hacked: action.data.hacked,
        uiState: HIDDEN,
    }
}