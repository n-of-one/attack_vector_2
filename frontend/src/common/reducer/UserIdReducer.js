import {SET_USER_ID} from "../enums/CommonActions";


const defaultState = {
    id: null
};

const userIdReducer = (state = defaultState, action) => {
    switch(action.type) {
        case SET_USER_ID : return action.userId;
        default: return state;
    }
};

export default userIdReducer
