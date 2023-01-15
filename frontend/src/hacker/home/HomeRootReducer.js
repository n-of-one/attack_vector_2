import {combineReducers} from 'redux'
import scansReducer from "./ScansReducer";

const homeRootReducer = combineReducers({
    scans: scansReducer,
});

export default homeRootReducer;