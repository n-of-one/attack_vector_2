import {AnyAction} from "redux";
import {NAVIGATE_PAGE} from "../../common/menu/pageReducerX";

export const SELECT_MAIL = "SELECT_MAIL";


// the state is the id of the selected mail, or null if no mail is selected.
const defaultState = null;

export const currentMailReducer = (state: string | undefined | null = defaultState, action: AnyAction) => {
    switch(action.type) {
        case SELECT_MAIL: return action.mailId
        case NAVIGATE_PAGE : return null
        default: return state
    }
}
