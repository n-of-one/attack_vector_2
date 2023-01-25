import {combineReducers} from 'redux'
import {ScanInfo, scansReducer} from "./ScansReducer";

export interface HomeState {
    scans: ScanInfo[]
}

export const homeRootReducer = combineReducers({
    scans: scansReducer,
});
