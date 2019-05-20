import {SERVER_RECEIVE_USER_SCANS} from "./HomeActions";

const defaultState = [];

export default (state = defaultState, action) => {
    switch(action.type) {
        case SERVER_RECEIVE_USER_SCANS : return action.data;
        default: return state;
    }
}
