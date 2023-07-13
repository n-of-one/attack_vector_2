import {AnyAction} from "redux";
import {IceStrength} from "../../../common/model/IceStrength";
import {IceAppEnter, SERVER_ENTER_ICE_APP} from "../IceAppServerActionProcessor";
import {IceType} from "../../../ice/IceModel";
import {TAR_ICE} from "../../../common/enums/LayerTypes";

export interface IceAppInfo {
    type: IceType,
    strength: IceStrength,
    hint?: string,
}

const defaultIceAppInfo = {
    type: TAR_ICE as "TAR_ICE",
    strength: IceStrength.AVERAGE,
    hint: "mother's name",
}

export const iceAppInfoReducer = (state : IceAppInfo = defaultIceAppInfo, action: AnyAction): IceAppInfo => {
    switch (action.type) {
        case SERVER_ENTER_ICE_APP:
            return processEnterIce(action.data)
        default: return state
    }
}

const processEnterIce = (data: IceAppEnter): IceAppInfo => {
    return {
        type: data.type,
        hint: data.hint,
        strength: data.strength
    }
}
