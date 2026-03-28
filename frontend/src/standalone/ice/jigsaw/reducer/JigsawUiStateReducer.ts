import {AnyAction} from "redux";
import {IceStrength} from "../../../../common/model/IceStrength";
import {HIDDEN, UiMode, VISIBLE} from "../../common/IceModel";
import {SERVER_JIGSAW_ENTER} from "../JigsawServerActionProcessor";

export const JIGSAW_BEGIN = "JIGSAW_BEGIN"

export interface JigsawUiState {
    strength: IceStrength,
    mode: UiMode,
}

const defaultState: JigsawUiState = {
    strength: IceStrength.UNKNOWN,
    mode: HIDDEN,
}

interface JigsawEnterFromServer {
    data: {
        strength: IceStrength,
    }
}

export const jigsawUiStateReducer = (state: JigsawUiState = defaultState, action: AnyAction): JigsawUiState => {
    switch (action.type) {
        case SERVER_JIGSAW_ENTER:
            return {...defaultState, strength: (action as unknown as JigsawEnterFromServer).data.strength}
        case JIGSAW_BEGIN:
            return {...state, mode: VISIBLE}
        default:
            return state
    }
}
