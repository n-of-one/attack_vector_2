import {NAVIGATE_PAGE, SERVER_NAVIGATE_PAGE} from "../enums/CommonActions";

const defaultState = "No page yet.";

export default (state = defaultState, action) => {
    switch(action.type) {
        case NAVIGATE_PAGE : return action.to;
        default: return state;
    }
}
