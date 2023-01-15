import {combineReducers} from 'redux'
import mailReducer from "./MailsReducer";
import currentMailReducer from "./CurrentMailReducer";

const mailRootReducer = combineReducers({
    mails: mailReducer,
    currentMail: currentMailReducer,
});

export default mailRootReducer;