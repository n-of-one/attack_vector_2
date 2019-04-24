import {SERVER_SITE_FULL, SERVER_UPDATE_SITE_DATA} from "../EditorActions";

let defaultSiteData = {
    id: null,
    name: "non name yet",
    description: "",
    creator: null,
    hackTime: "-",
    startNodeId: "-",
    hackable: false
};

export default (state = {defaultSiteData}, action) => {
    switch(action.type) {
        case SERVER_SITE_FULL: return action.data.siteData;
        case SERVER_UPDATE_SITE_DATA: return action.data;
        default: return state;
    }
}