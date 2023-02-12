import {AnyAction} from "redux"
import {SERVER_SITE_FULL, SERVER_UPDATE_SITE_DATA} from "../server/EditorServerActionProcessor"
import {SERVER_SCAN_FULL} from "../../hacker/server/RunServerActionProcessor";

export interface SiteProperties {
    siteId: string,
    name: string,
    description: string,
    creator: string
    hackTime: string
    startNodeNetworkId: string
    hackable: boolean
}


export const sitePropertiesDefault: SiteProperties = {
    siteId: "",
    name: "",
    description: "",
    creator: "",
    hackTime: "-",
    startNodeNetworkId: "00",
    hackable: false
}

export const SitePropertiesReducer = (state: SiteProperties = sitePropertiesDefault, action: AnyAction) => {
    switch (action.type) {
        case SERVER_SITE_FULL:
            return action.data.siteProperties
        case SERVER_SCAN_FULL:
            return action.data.site.siteProperties
        case SERVER_UPDATE_SITE_DATA:
            return action.data
        default:
            return state
    }
}



