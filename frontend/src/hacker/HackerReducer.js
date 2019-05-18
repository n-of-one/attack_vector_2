import {combineReducers} from 'redux'
import pageReducer from "../common/menu/pageReducer";
import ScansReducer from "./recuder/ScansReducer";
import MailReducer from "./mail/reducer/MailsReducer";
import CurrentMailReducer from "./mail/reducer/CurrentMailReducer";

const hackerReducer = combineReducers({
    currentPage: pageReducer,
    scans: ScansReducer,
    mails: MailReducer,
    currentMail: CurrentMailReducer,
});

export default hackerReducer;