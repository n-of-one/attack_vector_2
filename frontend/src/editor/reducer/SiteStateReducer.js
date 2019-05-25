import {SERVER_SITE_FULL, SERVER_UPDATE_SITE_STATE} from "../EditorActions";

const defaultState = {
    ok: true,
    messages: []
};

export default (state = defaultState, action) => {
    switch(action.type) {
        case SERVER_SITE_FULL: return action.data.state;
        case SERVER_UPDATE_SITE_STATE: return action.data;
        default: return state;
    }
};



