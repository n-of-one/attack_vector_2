import {combineReducers} from 'redux'
import pageReducer from "../common/menu/pageReducer";

const gmReducer = combineReducers({
    currentPage: pageReducer,
});

export default gmReducer;