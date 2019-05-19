import {NAVIGATE_PAGE, SERVER_NAVIGATE_PAGE} from "../enums/CommonActions";
import {SCAN} from "../../hacker/HackerPages";
import {ENTER_SCAN} from "../../hacker/home/HomeActions";

const defaultState = "No page yet.";

export default (state = defaultState, action) => {
    switch(action.type) {
        case NAVIGATE_PAGE : return action.target;
        // case ENTER_SCAN: return SCAN;
        case SERVER_NAVIGATE_PAGE : return action.data.target;
        default: return state;
    }
}
