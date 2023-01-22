import {SERVER_SITE_FULL, SERVER_UPDATE_SITE_STATE} from "../EditorActions";
import {AnyAction} from "redux";

export interface SiteStateI {
    ok: boolean,
    messages: SiteStateMessage[]
}

export enum SiteStateMessageType { ERROR = "ERROR", INFO = "INFO" }

export interface SiteStateMessage {
    type: SiteStateMessageType,
    text: String,
    nodeId: string | null,
    layerId: string | null
}

export const defaultState: SiteStateI = {
    ok: true,
    messages: []
};

const siteStateReducer = (state: SiteStateI = defaultState, action: AnyAction) => {
    switch(action.type) {
        case SERVER_SITE_FULL: return action.data.state;
        case SERVER_UPDATE_SITE_STATE: return action.data;
        default: return state;
    }
};

export default siteStateReducer;
