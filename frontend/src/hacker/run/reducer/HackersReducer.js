import {SERVER_SCAN_FULL} from "../model/ScanActions";

const defaultState = {};

const hackersReducer = (state = defaultState, action) => {
    switch (action.type) {
        case SERVER_SCAN_FULL:
            return action.data.hackers;
        default:
            return state;
    }
};


export default hackersReducer
