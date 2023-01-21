import {SERVER_SITE_FULL, SERVER_UPDATE_SITE_DATA} from "../EditorActions";
import {SERVER_SCAN_FULL} from "../../hacker/run/model/ScanActions";
import {AnyAction} from "redux";

export interface SiteData {
    siteId: string,
    name: string,
    description: string,
    creator: string | null
    hackTime: string
    startNodeNetworkId: string
    hackable: boolean
}


const siteDataDefaultState: SiteData = {
    siteId: "",
    name: "non name yet",
    description: "",
    creator: null,
    hackTime: "-",
    startNodeNetworkId: "00",
    hackable: false
};

const SiteDataReducer = (state: SiteData = siteDataDefaultState, action: AnyAction) => {
    switch (action.type) {
        case SERVER_SITE_FULL:
            return action.data.siteData;
        case SERVER_SCAN_FULL:
            return action.data.site.siteData;
        case SERVER_UPDATE_SITE_DATA:
            return action.data;
        default:
            return state;
    }
};

export {SiteDataReducer, siteDataDefaultState};



