import {AnyAction} from "redux";

export const RECEIVE_SITES = "RECEIVE_SITES";

export interface GmSite { id: string, name: string }

export const gmSitesReducer = (state: Array<GmSite> = [], action: AnyAction) => {
    switch (action.type) {
        case RECEIVE_SITES :
            return action.sites;
        default:
            return state;
    }
}
