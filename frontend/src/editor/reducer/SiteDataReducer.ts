import {SERVER_SCAN_FULL} from "../../hacker/run/model/ScanActions"
import {AnyAction} from "redux"
import {SERVER_SITE_FULL, SERVER_UPDATE_SITE_DATA} from "../server/EditorServerActionProcessor"

export interface SiteData {
    siteId: string,
    name: string,
    description: string,
    creator: string
    hackTime: string
    startNodeNetworkId: string
    hackable: boolean
}


export  const siteDataDefaultState: SiteData = {
    siteId: "",
    name: "non name yet",
    description: "",
    creator: "",
    hackTime: "-",
    startNodeNetworkId: "00",
    hackable: false
}

export const SiteDataReducer = (state: SiteData = siteDataDefaultState, action: AnyAction) => {
    switch (action.type) {
        case SERVER_SITE_FULL:
            return action.data.siteData
        case SERVER_SCAN_FULL:
            return action.data.site.siteData
        case SERVER_UPDATE_SITE_DATA:
            return action.data
        default:
            return state
    }
}



