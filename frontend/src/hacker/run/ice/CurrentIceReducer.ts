import {ICE_PASSWORD, ICE_TANGLE} from "../../../common/enums/LayerTypes";
import {FINISH_HACKING_ICE} from "../model/HackActions";
import {AnyAction} from "redux";
import {SERVER_START_HACKING_ICE_TANGLE} from "./tangle/TangleIceReducer";
import {SERVER_START_HACKING_ICE_PASSWORD} from "./password/PasswordIceReducer";

export interface CurrentIce {
    layerId: string | null,
    type: string | null,
}

export const currentIceDefault = {
    layerId: null,
    type: null
}


export const currentIceReducer = (state: CurrentIce = currentIceDefault, action: AnyAction) => {
    switch (action.type) {
        case SERVER_START_HACKING_ICE_PASSWORD:
            return { layerId: action.data.layerId, type: ICE_PASSWORD }
        case SERVER_START_HACKING_ICE_TANGLE:
            return { layerId: action.data.layerId, type: ICE_TANGLE }
        case FINISH_HACKING_ICE:
            return currentIceDefault
        default:
            return state;
    }
}
