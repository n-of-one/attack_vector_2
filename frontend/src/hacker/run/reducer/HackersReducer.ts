import {AnyAction} from "redux";
import {SERVER_ENTER_RUN} from "../../server/RunServerActionProcessor";

export const HACKER_ACTIVITY_SCANNING = "SCANNING";

export type HackerActivity = "NA" | "SCANNING" | "ATTACKING";

export interface HackerPresence {
    userId: string,
    userName: string,
    icon: string,
    nodeId?: string,
    activity: HackerActivity,
    masked: boolean
}


const defaultState: HackerPresence[] = [];

export const hackersReducer = (state: HackerPresence[] = defaultState, action: AnyAction): HackerPresence[] => {
    switch (action.type) {
        case SERVER_ENTER_RUN:
            return action.data.hackers;
        default:
            return state;
    }
};