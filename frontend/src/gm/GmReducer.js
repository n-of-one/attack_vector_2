import {combineReducers} from 'redux'
import pageReducer from "../common/menu/pageReducer";
import sitesReducer from "./GmSitesReducer"

const gmReducer = combineReducers({
    currentPage: pageReducer,
    sites: sitesReducer,
});

export default gmReducer;