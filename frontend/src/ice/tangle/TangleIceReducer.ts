import {AnyAction} from "redux";
import {HIDDEN, UNLOCKED} from "../IceModel";

export const ICE_TANGLE_BEGIN = "ICE_TANGLE_BEGIN";
export const SERVER_ENTER_ICE_TANGLE = "SERVER_ENTER_ICE_TANGLE";
export const SERVER_TANGLE_POINT_MOVED = "SERVER_TANGLE_POINT_MOVED";

export interface TangleIceState {
    layerId: string,
    strength: string,
    points: TanglePoint[],
    lines: TangleLine[],
    uiState: string,
}

export interface TanglePoint {
    id: string,
    x: number,
    y: number
}

export interface TangleLine {
    id: string,
    fromId: string,
    toId: string,
    type: string
}

export const defaultState: TangleIceState = {
    layerId: "",
    strength: "AVERAGE",
    uiState: HIDDEN,
    points: [],
    lines: []
};

export const tangleIceReducer = (state: TangleIceState = defaultState, action: AnyAction): TangleIceState => {

    switch (action.type) {
        case SERVER_ENTER_ICE_TANGLE:
            return {...action.data, uiState: HIDDEN};
        case ICE_TANGLE_BEGIN:
            return {...state, uiState: UNLOCKED};
        default:
            return state;
    }
};
