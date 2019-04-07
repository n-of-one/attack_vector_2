import {NAVIGATE_PAGE} from "../CommonActions";

export default (state = "loading", action) => {
    switch(action.type) {
        case NAVIGATE_PAGE : return action.target;
        default: return state;
    }
}
