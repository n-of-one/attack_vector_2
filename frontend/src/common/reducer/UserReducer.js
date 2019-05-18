import {SET_USER_ID} from "../enums/CommonActions";


const defaultState = {
    id: null
};

export default (state = defaultState, action) => {
    switch(action.type) {
        case SET_USER_ID : return { id: action.userId };
        default: return state;
    }
}
