import {combineReducers} from 'redux'
import pageReducer from "../common/menu/pageReducer";
import ScansReducer from "./recuder/ScansReducer";
import MailReducer from "./mail/MailsReducer";
import CurrentMailReducer from "./mail/CurrentMailReducer";

const hackerReducer = combineReducers({
    currentPage: pageReducer,
    scans: ScansReducer,
    mails: MailReducer,
    currentMail: CurrentMailReducer,
});

export default hackerReducer;