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
    ingressNodeId?: string, // where the hacker connected to the site: absent if they are outside and not connecting
    activity: HackerActivity,
}

const defaultState: HackerPresence[] = [];

export const hackerPresencesReducer = (state: HackerPresence[] = defaultState, action: AnyAction): HackerPresence[] => {
    switch (action.type) {
        case SERVER_ENTERED_RUN:
            return action.data.hackers;
        default:
            return state;
    }
};
