import {combineReducers} from 'redux'
import MailReducer from "./MailsReducer";
import CurrentMailReducer from "./CurrentMailReducer";

const mailRootReducer = combineReducers({
    mails: MailReducer,
    currentMail: CurrentMailReducer,
});

export default mailRootReducer;