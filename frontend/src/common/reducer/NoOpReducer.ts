import {AnyAction} from "redux";

export const noOpStringReducer = (state: string = "", action: AnyAction): string => {
    return state
}

export const noOpStringOrNullReducer =  (state: string | null = null, action: AnyAction): string | null=> {
    return state
}