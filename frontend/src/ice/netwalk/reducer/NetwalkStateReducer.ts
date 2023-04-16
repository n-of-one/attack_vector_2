import {AnyAction} from "redux";
import {IceStrength} from "../../../common/model/IceStrength";
import {ServerEnterIceNetwalk} from "../NetwalkServerActionProcessor";


export const SERVER_ENTER_ICE_NETWALK = "SERVER_ENTER_ICE_NETWALK"
export const SERVER_NETWALK_NODE_ROTATED = "SERVER_NETWALK_NODE_ROTATED"


/*
Cell name indicates which directions are free to go. If the cell type is N then you can only go North.
 */



export interface NetwalkState {
    strength: IceStrength,
    hacked: boolean,
}

const defaultState: NetwalkState = {
    strength: IceStrength.AVERAGE,
    hacked: false
}

export const netwalkStateReducer = (state: NetwalkState = defaultState, action: AnyAction): NetwalkState => {

    switch (action.type) {
        case SERVER_ENTER_ICE_NETWALK:
            return enter(state, action as unknown as NetwalkEnterFromServer)
        default:
            return state
    }
}

interface NetwalkEnterFromServer {
    data: ServerEnterIceNetwalk
}

const enter =  (state: NetwalkState, action: NetwalkEnterFromServer): NetwalkState => {
    return {
        strength: action.data.strength,
        hacked: action.data.hacked
    }
}
