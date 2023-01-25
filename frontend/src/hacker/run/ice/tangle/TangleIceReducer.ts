import {HIDDEN, UNLOCKED} from "../IceUiState";
import {ICE_TANGLE_BEGIN, SERVER_START_HACKING_ICE_TANGLE} from "./TangleIceActions";
import {ICE_TANGLE} from "../../../../common/enums/LayerTypes";
import {AnyAction} from "redux";
import {CurrentIce} from "../CurrentIceReducer";

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

const defaultState: TangleIceState = {
    layerId: "",
    strength: "AVERAGE",
    uiState: HIDDEN,
    points: [],
    lines: []
};

export const tangleIceReducer = (state: TangleIceState = defaultState, action: AnyAction, currentIce: CurrentIce) => {
    if (!currentIce || currentIce.type !== ICE_TANGLE) {
        return
    }

    switch (action.type) {
        case SERVER_START_HACKING_ICE_TANGLE:
            return {...action.data, uiState: HIDDEN};
        case ICE_TANGLE_BEGIN:
            return {...state, uiState: UNLOCKED};
        default:
            return state;
    }
};
