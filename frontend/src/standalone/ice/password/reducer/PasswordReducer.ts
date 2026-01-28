import {HIDDEN, VISIBLE} from "../../common/IceModel";

export const ICE_PASSWORD_BEGIN = "ICE_PASSWORD_BEGIN"

export interface PasswordIceHackUi {
    state: "HIDDEN" | "VISIBLE"
}

const defaultState: PasswordIceHackUi = {
    state: HIDDEN
}

export const passwordIceHackUiReducer = (state: PasswordIceHackUi = defaultState, action: any): PasswordIceHackUi => {

    switch (action.type) {
        case ICE_PASSWORD_BEGIN:
            return { state: VISIBLE }
        default:
            return state
    }
}
