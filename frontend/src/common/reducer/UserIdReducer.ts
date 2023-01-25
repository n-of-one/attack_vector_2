import {SET_USER_ID} from "../enums/CommonActions";
import {AnyAction} from "redux";

export interface UserIdState {
    id: string | null
}

const defaultState = {
    id: null
};

export const userIdReducer = (state: UserIdState = defaultState, action: AnyAction): UserIdState => {
    switch(action.type) {
        case SET_USER_ID : return action.userId;
        default: return state;
    }
};
