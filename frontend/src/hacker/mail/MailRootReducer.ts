import {combineReducers} from 'redux'
import {mailReducer, MailState} from "./MailsReducer";
import {currentMailReducer} from "./CurrentMailReducer";

export interface MailRootState {
    mails:MailState,
    currentMail: string | null
}

export const mailRootReducer = combineReducers({
    mails: mailReducer,
    currentMail: currentMailReducer,
});
