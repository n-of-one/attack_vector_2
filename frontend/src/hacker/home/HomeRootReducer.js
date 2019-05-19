import {combineReducers} from 'redux'
import ScansReducer from "./ScansReducer";

const homeRootReducer = combineReducers({
    scans: ScansReducer,
});

export default homeRootReducer;