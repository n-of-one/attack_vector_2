import {SERVER_SCAN_FULL} from "../model/ScanActions";
import {AnyAction} from "redux";

export interface HackerPresence {
    userId: string,
    userName: string,
    icon: string,
    nodeId?: string,
    targetNodeId?: string,
    activity: RunActivity,
    locked: boolean
}

export type RunActivity = "NA" | "SCANNING" | "STARTING" | "AT_NODE" | "MOVING";

const defaultState: HackerPresence[] = [];

export const hackersReducer = (state: HackerPresence[] = defaultState, action: AnyAction): HackerPresence[] => {
    switch (action.type) {
        case SERVER_SCAN_FULL:
            return action.data.hackers;
        default:
            return state;
    }
};