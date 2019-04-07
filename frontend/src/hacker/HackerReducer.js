import {combineReducers} from 'redux'
import pageReducer from "../common/reducer/pageReducer";

const hackerReducer = combineReducers({
    currentPage: pageReducer,
});

export default hackerReducer;