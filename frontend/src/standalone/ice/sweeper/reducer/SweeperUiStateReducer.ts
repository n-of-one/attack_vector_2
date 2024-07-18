import {AnyAction} from "redux";
import {IceStrength} from "../../../../common/model/IceStrength";
import {HIDDEN, UiMode, VISIBLE} from "../../common/IceModel";
import {SweeperGameState} from "../SweeperModel";

export const SERVER_SWEEPER_ENTER = "SERVER_SWEEPER_ENTER"
export const SERVER_SWEEPER_NODE_CHANGED = "SERVER_SWEEPER_NODE_CHANGED"
export const SWEEPER_BEGIN = "SWEEPER_BEGIN"


export interface SweeperUiState {
    strength: IceStrength,
    mode: UiMode,
    hacked: boolean,
}

const defaultState: SweeperUiState = {
    strength: IceStrength.UNKNOWN,
    hacked: false,
    mode: HIDDEN,
}

export const sweeperUiStateReducer = (state: SweeperUiState = defaultState, action: AnyAction): SweeperUiState => {

    switch (action.type) {
        case SERVER_SWEEPER_ENTER:
            return enter(action as unknown as NetwalkEnterFromServer)
        case SWEEPER_BEGIN:
            return { ...state, mode: VISIBLE }
        default:
            return state
    }
}

interface NetwalkEnterFromServer {
    data: SweeperGameState
}

const enter =  (action: NetwalkEnterFromServer): SweeperUiState => {

    return {
        strength: action.data.strength,
        hacked: action.data.hacked,
        mode: HIDDEN,
    }
}
