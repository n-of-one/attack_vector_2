import {AnyAction} from "redux";
import {IceStrength} from "../../../../common/model/IceStrength";
import {ServerEnterIceNetwalk} from "../NetwalkServerActionProcessor";

export const SERVER_NETWALK_ENTER = "SERVER_NETWALK_ENTER"
export const SERVER_NETWALK_NODE_ROTATED = "SERVER_NETWALK_NODE_ROTATED"
export const NETWALK_BEGIN = "NETWALK_BEGIN"

export interface NetwalkState {
    strength: IceStrength,
    uiState: "HIDDEN" | "UNLOCKED"
}

const defaultState: NetwalkState = {
    strength: IceStrength.AVERAGE,
    uiState: "HIDDEN"
}

export const netwalkStateReducer = (state: NetwalkState = defaultState, action: AnyAction): NetwalkState => {

    switch (action.type) {
        case SERVER_NETWALK_ENTER:
            return enter(action as unknown as NetwalkEnterFromServer)
        case NETWALK_BEGIN:
            return { ...state, uiState: "UNLOCKED" }
        default:
            return state
    }
}

interface NetwalkEnterFromServer {
    data: ServerEnterIceNetwalk
}

const enter =  (action: NetwalkEnterFromServer): NetwalkState => {
    return {
        strength: action.data.strength,
        uiState: "HIDDEN"
    }
}
