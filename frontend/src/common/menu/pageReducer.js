import {NAVIGATE_PAGE} from "../enums/CommonActions";

const defaultState = "No page yet.";

const pageReducer = (state = defaultState, action) => {
    switch(action.type) {
        case NAVIGATE_PAGE : return action.to;
        default: return state;
    }
}

export default pageReducer;
