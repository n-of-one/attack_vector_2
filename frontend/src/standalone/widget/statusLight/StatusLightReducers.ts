import {AnyAction} from "redux";

const SERVER_STATUS_LIGHT_UPDATE = "SERVER_STATUS_LIGHT_UPDATE"

export interface StatusLightState {
    status: boolean | null,
    textForRed: string,
    textForGreen: string
}

export const defaultState = {
    status: null,
    textForRed: "",
    textForGreen: "",
}

export const statusLightReducer = (state: StatusLightState = defaultState, action: AnyAction): StatusLightState => {
    switch (action.type) {
        case SERVER_STATUS_LIGHT_UPDATE:
            return action.data
        default:
            return state
    }
}
