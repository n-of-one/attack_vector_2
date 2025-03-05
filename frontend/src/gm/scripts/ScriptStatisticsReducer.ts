import {AnyAction} from "redux";

const SERVER_SCRIPT_STATISTICS = "SERVER_SCRIPT_STATISTICS"

export interface ScriptStatistics {
    name: string,
    owned: number,
    loaded: number,
    freeReceive: number,
}


export const scriptStatisticsReducer = (state: ScriptStatistics[] = [], action: AnyAction): ScriptStatistics[] => {
    switch (action.type) {
        case SERVER_SCRIPT_STATISTICS:
            return action.data
        default:
            return state
    }
}
