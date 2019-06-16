import {SERVER_SITE_FULL, SERVER_UPDATE_SITE_DATA} from "../EditorActions";
import {SERVER_SCAN_FULL} from "../../hacker/run/model/ScanActions";

const siteDataDefaultState = {
    id: null,
    name: "non name yet",
    description: "",
    creator: null,
    hackTime: "-",
    startNodeNetworkId: "00",
    hackable: false
};

const SiteDataReducer  = (state = {siteDataDefaultState}, action) => {
    switch(action.type) {
        case SERVER_SITE_FULL: return action.data.siteData;
        case SERVER_SCAN_FULL: return action.data.site.siteData;
        case SERVER_UPDATE_SITE_DATA: return action.data;
        default: return state;
    }
};

export {SiteDataReducer, siteDataDefaultState };



