import {combineReducers} from 'redux'
import pageReducer from "../common/reducer/pageReducer";

import terminalReducer from "../common/component/terminal/TerminalReducer"

const hackerReducer = combineReducers({
    currentPage: pageReducer,
    terminal: terminalReducer
});

export default hackerReducer;