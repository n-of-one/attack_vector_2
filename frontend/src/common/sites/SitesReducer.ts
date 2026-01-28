import {AnyAction} from "redux"

export const SERVER_SITES_LIST = "SERVER_SITES_LIST"

export interface SiteInfo {
    id: string,
    name: string,
    ownerName: string,
    purpose: string,
    hackable: boolean,
    ok: boolean,
    mine: boolean,
    gmSite: boolean,
}

export const sitesReducer = (state: Array<SiteInfo> = [], action: AnyAction) => {
    switch (action.type) {
        case SERVER_SITES_LIST :
            return action.data
        default:
            return state
    }
}
