import {AnyAction} from "redux";
import {HIDDEN, VISIBLE} from "../../common/IceModel";
import {IceStrength} from "../../../../common/model/IceStrength";

export const ICE_TANGLE_BEGIN = "ICE_TANGLE_BEGIN";
export const SERVER_TANGLE_ENTER = "SERVER_TANGLE_ENTER";
export const SERVER_TANGLE_POINT_MOVED = "SERVER_TANGLE_POINT_MOVED";


export interface TangleIceState {
    layerId: string,
    strength: IceStrength,
    points: TanglePoint[],
    lines: TangleLine[],
    uiState: string,
    clusters: number,
    clustersRevealed: boolean,
}

export interface TanglePoint {
    id: string,
    x: number,
    y: number,
    cluster: number
}

export interface TangleLine {
    id: string,
    fromId: string,
    toId: string,
    type: string
}

export const defaultState: TangleIceState = {
    layerId: "",
    strength: IceStrength.UNKNOWN,
    uiState: HIDDEN,
    points: [],
    lines: [],
    clusters: 0,
    clustersRevealed: false,
};

export const tangleIceReducer = (state: TangleIceState = defaultState, action: AnyAction): TangleIceState => {

    switch (action.type) {
        case SERVER_TANGLE_ENTER:
            return {...action.data, uiState: HIDDEN, clusters: action.data.clusters};
        case ICE_TANGLE_BEGIN:
            return {...state, uiState: VISIBLE};
        default:
            return state;
    }
};
