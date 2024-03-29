import {IceStrength} from "../../../../common/model/IceStrength";
import {HIDDEN, VISIBLE} from "../../common/IceModel";
import {SERVER_TAR_ENTER} from "../TarServerActionProcessor";

export const TAR_BEGIN = "TAR_BEGIN"


export interface TarState {
    strength: IceStrength,
    hacked: boolean,
    uiState: "HIDDEN" | "VISIBLE",
}

const defaultState: TarState = {
    strength: IceStrength.AVERAGE,
    hacked: false,
    uiState: HIDDEN,
}

export const tarStateReducer = (state: TarState = defaultState, action: any): TarState => {

        switch (action.type) {
            case SERVER_TAR_ENTER:
                return enter(state, action)
            case TAR_BEGIN:
                return { ...state, uiState: VISIBLE }
            default:
                return state
        }
}

const enter =  (state: TarState, action: any): TarState => {
    return {
        strength: action.data.strength,
        hacked: action.data.hacked,
        uiState: HIDDEN,
    }
}