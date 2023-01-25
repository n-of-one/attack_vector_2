import {SELECT_MAIL} from "./MailActions";
import {AnyAction} from "redux";

// the state is the id of the selected mail, or null if no mail is selected.
const defaultState = null;

export const currentMailReducer = (state: string | undefined | null = defaultState, action: AnyAction) => {
    switch(action.type) {
        case SELECT_MAIL: return action.mailId;
        default: return state;
    }
}
