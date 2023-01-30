import {SET_USER_ID} from "../enums/CommonActions";
import {AnyAction} from "redux";


export const userIdReducer = (state: string = "unknown", action: AnyAction): string => {
    switch(action.type) {
        case SET_USER_ID : return action.userId;
        default: return state;
    }
};
