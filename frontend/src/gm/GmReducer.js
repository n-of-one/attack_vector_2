import {combineReducers} from 'redux'
import pageReducer from "../common/menu/pageReducer";
import gmSitesReducer from "./GmSitesReducer"

const gmReducer = combineReducers({
    currentPage: pageReducer,
    sites: gmSitesReducer,
});

export default gmReducer;