import {SERVER_RECEIVE_USER_SCANS, SERVER_UPDATE_SCAN_INFO} from "./HomeActions";
import {updateArrayById} from "../../common/Immutable";

const defaultState = [];

export default (state = defaultState, action) => {
    switch(action.type) {
        case SERVER_RECEIVE_USER_SCANS : return action.data;
        case SERVER_UPDATE_SCAN_INFO: return processScanInfoUpdate(state, action.data);
        default: return state;
    }
}

const processScanInfoUpdate = (state, scanInfo) => {
    return updateArrayById(scanInfo, state, scanInfo.runId, "runId")
};
