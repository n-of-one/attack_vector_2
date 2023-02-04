import {AnyAction} from "redux"

export const SET_USER_ID = "SET_USER_ID"

export const userIdReducer = (state: string = "unknown", action: AnyAction): string => {
    switch(action.type) {
        case SET_USER_ID : return action.userId
        default: return state
    }
}
