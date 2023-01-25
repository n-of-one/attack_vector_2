import {combineReducers} from 'redux'
import scansReducer from "./ScansReducer";
import {Scan} from "../run/reducer/ScanReducer";


export interface HomeState {
    scans: Scan[]
}

export const homeRootReducer = combineReducers({
    scans: scansReducer,
});
