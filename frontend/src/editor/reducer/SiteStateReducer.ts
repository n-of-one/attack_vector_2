import {AnyAction} from "redux";
import {SERVER_SITE_FULL, SERVER_UPDATE_SITE_STATE} from "../server/EditorServerActionProcessor"

export interface SiteStateI {
    messages: SiteStateMessage[]
}

export enum SiteStateMessageType { ERROR = "ERROR", INFO = "INFO" }

export interface SiteStateMessage {
    type: SiteStateMessageType,
    text: string,
    nodeId: string | null,
    layerId: string | null
}

export const siteStateDefault: SiteStateI = {
    messages: []
};

export const siteStateReducer = (state: SiteStateI = siteStateDefault, action: AnyAction) => {
    switch(action.type) {
        case SERVER_SITE_FULL: return action.data.state
        case SERVER_UPDATE_SITE_STATE: return action.data
        default: return state
    }
}
