import {SERVER_SITE_FULL, SERVER_UPDATE_SITE_DATA} from "../EditorActions";

export default (state = {id: null, link: "loading"}, action) => {
    switch(action.type) {
        case SERVER_SITE_FULL: return action.data.site;
        case SERVER_UPDATE_SITE_DATA: return action.data;
        default: return state;
    }
}
