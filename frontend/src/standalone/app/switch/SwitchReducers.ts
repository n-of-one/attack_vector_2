import {AnyAction, combineReducers} from "redux";

const SERVER_STATUS_LIGHT_UPDATE = "SERVER_STATUS_LIGHT_UPDATE"


export interface SwitchState {
    status: boolean | null,
    textForRed: string,
    textForGreen: string
}

const defaultState = {
    status: null,
    textForRed: "",
    textForGreen: "",
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

export const swithRootReducer = combineReducers<SwitchRootState>({
    switch: switchReducer
})

