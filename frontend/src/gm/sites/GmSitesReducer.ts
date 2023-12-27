import {AnyAction} from "redux"

export const SERVER_SITES_LIST = "SERVER_SITES_LIST"

export interface GmSite {
    id: string,
    name: string,
    creator: string,
    hackable: boolean
}

export const gmSitesReducer = (state: Array<GmSite> = [], action: AnyAction) => {
    switch (action.type) {
        case SERVER_SITES_LIST :
            return action.data
        default:
            return state
    }
}
