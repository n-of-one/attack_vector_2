import {AnyAction} from "redux";
import {IceStrength} from "../../../../common/model/IceStrength";
import {AuthEnter, SERVER_AUTH_ENTER} from "../AuthServerActionProcessor";
import {IceType} from "../../../../common/enums/LayerTypes";

export interface AuthAppInfo {
    type?: IceType,
    strength: IceStrength,
    hint?: string,
}

const defaultAuthAppInfo = {
    type: undefined,
    strength: IceStrength.AVERAGE,
    hint: "mother's name",
}

export const authInfoReducer = (state : AuthAppInfo = defaultAuthAppInfo, action: AnyAction): AuthAppInfo => {
    switch (action.type) {
        case SERVER_AUTH_ENTER:
            return processEnterIce(action.data)
        default: return state
    }
}

const processEnterIce = (data: AuthEnter): AuthAppInfo => {
    return {
        type: data.type,
        hint: data.hint,
        strength: data.strength
    }
}
