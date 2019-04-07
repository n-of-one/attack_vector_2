import {combineReducers} from 'redux'
import pageReducer from "../common/reducer/pageReducer";

const gmReducer = combineReducers({
    currentPage: pageReducer,
});

export default gmReducer;