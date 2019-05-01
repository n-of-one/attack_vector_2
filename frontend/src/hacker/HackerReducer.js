import {combineReducers} from 'redux'
import pageReducer from "../common/reducer/pageReducer";
import ScansReducer from "./recuder/ScansReducer";

const hackerReducer = combineReducers({
    currentPage: pageReducer,
    scans: ScansReducer
});

export default hackerReducer;