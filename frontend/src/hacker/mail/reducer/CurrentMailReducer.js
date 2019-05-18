// import {RECEIVE_SCANS} from "../HackerActions";

import {SELECT_MAIL} from "../MailActions";

// the state is the id of the selected mail, or null if no mail is selected.
const defaultState = null;

export default (state = defaultState, action) => {
    switch(action.type) {
        case SELECT_MAIL: return action.mailId;
        default: return state;
    }
}
