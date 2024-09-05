import {AnyAction} from "redux"
import {SERVER_SITE_FULL, SERVER_UPDATE_SITE_DATA} from "../server/EditorServerActionProcessor"
import {SERVER_ENTERED_RUN} from "../../hacker/RunServerActionProcessor";

export interface SiteProperties {
    siteId: string,
    name: string,
    description: string,
    purpose: string,
    ownerName: string,
    startNodeNetworkId: string
    hackable: boolean
    shutdownEnd: string | null
    siteStructureOk: boolean
}


export const sitePropertiesDefault: SiteProperties = {
    siteId: "",
    name: "",
    description: "",
    purpose: "",
    ownerName: "",
    startNodeNetworkId: "",
    hackable: false,
    shutdownEnd: null, // "2023-10-15T12:24:53.259+02:00"
    siteStructureOk: true
}

export const SitePropertiesReducer = (state: SiteProperties = sitePropertiesDefault, action: AnyAction) => {
    switch (action.type) {
        case SERVER_SITE_FULL:
            return parseSiteProperties(action)
        case SERVER_ENTERED_RUN:
            return action.data.site.siteProperties
        case SERVER_UPDATE_SITE_DATA:
            return updateSiteData(state, action)
        default:
            return state
    }
}

const parseSiteProperties = (action: AnyAction): SiteProperties => {
    const siteProperties = action.data.siteProperties
    siteProperties.ownerName = action.data.ownerName
    return siteProperties
}

const updateSiteData = (previousState: SiteProperties, action: AnyAction): SiteProperties => {
    const siteProperties = action.data
    siteProperties.ownerName = previousState.ownerName
    return siteProperties
}



