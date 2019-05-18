import {SERVER_SCAN_FULL} from "../model/ScanActions";

const defaultState = {

};

export default (state = defaultState, action) => {
    switch(action.type) {
        case SERVER_SCAN_FULL: return action.data.scan;
        default: return state;
    }
}
