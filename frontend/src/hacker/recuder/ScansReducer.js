import {RECEIVE_SCANS} from "../HackerActions";

const defaultState = [];

export default (state = defaultState, action) => {
    switch(action.type) {
        case RECEIVE_SCANS : return action.data;
        default: return state;
    }
}
