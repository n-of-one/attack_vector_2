import {AnyAction} from "redux"
import {SERVER_SITE_FULL, SERVER_UPDATE_SITE_DATA} from "../server/EditorServerActionProcessor"
import {SERVER_ENTER_RUN} from "../../hacker/server/RunServerActionProcessor";

export interface SiteProperties {
    siteId: string,
    name: string,
    description: string,
    creator: string
    hackTime: string
    startNodeNetworkId: string
    hackable: boolean
    shutdownEnd: string | null
}


export const sitePropertiesDefault: SiteProperties = {
    siteId: "",
    name: "",
    description: "",
    creator: "",
    hackTime: "-",
    startNodeNetworkId: "00",
    hackable: false,
    shutdownEnd: null // "2023-10-15T12:24:53.259+02:00"
}

export const SitePropertiesReducer = (state: SiteProperties = sitePropertiesDefault, action: AnyAction) => {
    switch (action.type) {
        case SERVER_SITE_FULL:
            return action.data.siteProperties
        case SERVER_ENTER_RUN:
            return action.data.site.siteProperties
        case SERVER_UPDATE_SITE_DATA:
            return action.data
        default:
            return state
    }
}



