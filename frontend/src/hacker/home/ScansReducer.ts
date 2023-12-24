import {updateArrayById} from "../../common/util/Immutable";
import {AnyAction} from "redux";

export const SERVER_UPDATE_USER_RUNS = "SERVER_UPDATE_USER_RUNS";
export const SERVER_UPDATE_RUN_INFO = "SERVER_UPDATE_RUN_INFO";

export interface ScanInfo {
    runId: string,
    siteName: string,
    siteId: string,
    nodes: string,
}


const defaultState: ScanInfo[] = [];

export const scansReducer = (state: ScanInfo[] = defaultState, action: AnyAction): ScanInfo[] => {
    switch(action.type) {
        case SERVER_UPDATE_USER_RUNS : return action.data
        case SERVER_UPDATE_RUN_INFO: return processScanInfoUpdate(state, action.data);
        default: return state
    }
}

const processScanInfoUpdate = (state: ScanInfo[], scanInfo: ScanInfo) => {
    return updateArrayById(scanInfo, state, scanInfo.runId, "runId")
}
