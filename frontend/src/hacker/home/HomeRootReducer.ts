import {combineReducers} from 'redux'
import {scansReducer, SiteInfo} from "./ScansReducer";

export interface HomeState {
    scans: SiteInfo[]
}

export const homeRootReducer = combineReducers({
    scans: scansReducer,
});
