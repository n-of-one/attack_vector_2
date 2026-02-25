import {AnyAction, combineReducers} from "redux";
import {StatusLightOption} from "../../widget/statusLight/StatusLightReducers";

const SERVER_STATUS_LIGHT_UPDATE = "SERVER_STATUS_LIGHT_UPDATE"


export interface SwitchState {
    switchLabel: string,
    currentOption: number | null,
    options: StatusLightOption[],
}

const defaultState = {
    switchLabel: "",
    currentOption: null,
    options: [],
}

export const switchReducer = (state: SwitchState = defaultState, action: AnyAction): SwitchState => {
    switch (action.type) {
        case SERVER_STATUS_LIGHT_UPDATE:
            return action.data
        default:
            return state
    }
}

export interface SwitchRootState {
    switch: SwitchState,
}

export const switchRootReducer = combineReducers<SwitchRootState>({
    switch: switchReducer
})

