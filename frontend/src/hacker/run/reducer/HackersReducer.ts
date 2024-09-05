import {AnyAction} from "redux";
import {SERVER_ENTERED_RUN} from "../../RunServerActionProcessor";


export enum HackerActivity {
    OFFLINE = "OFFLINE",
    ONLINE = "ONLINE",
    OUTSIDE = "OUTSIDE",
    INSIDE = "INSIDE"
}

export interface HackerPresence {
    userId: string,
    userName: string,
    icon: string,
    nodeId?: string,
    activity: HackerActivity,
}


const defaultState: HackerPresence[] = [];

export const hackersReducer = (state: HackerPresence[] = defaultState, action: AnyAction): HackerPresence[] => {
    switch (action.type) {
        case SERVER_ENTERED_RUN:
            return action.data.hackers;
        default:
            return state;
    }
};
