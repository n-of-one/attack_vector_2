import {AnyAction} from "redux";
import {IceStrength} from "../../../common/model/IceStrength";
import {AuthAppEnter, SERVER_AUTH_APP_ENTER} from "../AuthAppServerActionProcessor";
import {IceType} from "../../../ice/IceModel";

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

export const authAppInfoReducer = (state : AuthAppInfo = defaultAuthAppInfo, action: AnyAction): AuthAppInfo => {
    switch (action.type) {
        case SERVER_AUTH_APP_ENTER:
            return processEnterIce(action.data)
        default: return state
    }
}

const processEnterIce = (data: AuthAppEnter): AuthAppInfo => {
    return {
        type: data.type,
        hint: data.hint,
        strength: data.strength
    }
}
