import {NAVIGATE_PAGE, SERVER_NAVIGATE_PAGE} from "../CommonActions";

const defaultState = "No page yet.";

export default (state = defaultState, action) => {
    switch(action.type) {
        case NAVIGATE_PAGE : return action.target;
        case SERVER_NAVIGATE_PAGE : return action.data.target;
        default: return state;
    }
}
