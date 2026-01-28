import {AnyAction} from "redux";
import {SERVER_ALL_CORE_INFO} from "../server/EditorServerActionProcessor"

export interface CoreInfo {
    layerId: string,
    level: number,       // height of the layer. level 0 is always OS. Hack top level first.
    name: string,
    networkId: string
    siteId: string,
}

export const allCoresReducer = (state: CoreInfo[] = [], action: AnyAction) => {
    switch (action.type) {
        case SERVER_ALL_CORE_INFO:
            return action.data
        default:
            return state
    }
}


