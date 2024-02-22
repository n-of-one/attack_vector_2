import {updateArrayById} from "../../common/util/Immutable";
import {AnyAction} from "redux";

export const SERVER_UPDATE_USER_RUNS = "SERVER_UPDATE_USER_RUNS";
export const SERVER_UPDATE_RUN_INFO = "SERVER_UPDATE_RUN_INFO";

export interface RunInfo {
    runId: string,
    siteName: string,
    siteId: string,
    nodes: string,
}


const defaultState: RunInfo[] = [];

export const hackerRunsReducer = (state: RunInfo[] = defaultState, action: AnyAction): RunInfo[] => {
    switch(action.type) {
        case SERVER_UPDATE_USER_RUNS : return action.data
        case SERVER_UPDATE_RUN_INFO: return processScanInfoUpdate(state, action.data);
        default: return state
    }
}

const processScanInfoUpdate = (state: RunInfo[], scanInfo: RunInfo) => {
    return updateArrayById(scanInfo, state, scanInfo.runId, "runId")
}
