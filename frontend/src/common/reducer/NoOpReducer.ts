import {AnyAction} from "redux";

export const noOpStringReducer = (state: string = "", action: AnyAction): string => {
    return state
}