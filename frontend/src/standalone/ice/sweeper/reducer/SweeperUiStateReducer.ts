import {AnyAction} from "redux";
import {IceStrength} from "../../../../common/model/IceStrength";
import {HIDDEN, UiMode, VISIBLE} from "../../common/IceModel";
import {TICK} from "../../../../common/terminal/TerminalReducer";
import {
    SERVER_SWEEPER_BLOCK_USER,
    SERVER_SWEEPER_MODIFY,
    SweeperModifyAction,
    SweeperModifyData
} from "../SweeperServerActionProcessor";
import {SweeperGameState} from "../logic/SweeperLogic";

export const SERVER_SWEEPER_ENTER = "SERVER_SWEEPER_ENTER"
export const SWEEPER_BEGIN = "SWEEPER_BEGIN"
export const SWEEPER_RESET_START = "SWEEPER_RESET_START"
export const SWEEPER_RESET_STOP = "SWEEPER_RESET_STOP"
export const SERVER_SWEEPER_SOLVED = "SERVER_SWEEPER_SOLVED"

export enum SweeperResetState {
    IDLE,
    IN_PROGRESS,
}

export interface SweeperUiState {
    strength: IceStrength,
    mode: UiMode,
    hacked: boolean,
    resetState: SweeperResetState,
    resetProgress: number,
    resetStartMillis: number,
    blockedUserIds: string[],
    minesLeft: number | null,
}

const defaultState: SweeperUiState = {
    strength: IceStrength.UNKNOWN,
    hacked: false,
    mode: HIDDEN,
    resetState: SweeperResetState.IDLE,
    resetProgress: 0,
    resetStartMillis: 0,
    blockedUserIds: [],
    minesLeft: null,
}

export const RESET_MILLIS = 7* 1000


export const sweeperUiStateReducer = (state: SweeperUiState = defaultState, action: AnyAction): SweeperUiState => {

    if (action.type === TICK && state.resetState === SweeperResetState.IN_PROGRESS) {
        const currentMillis = Date.now()
        const elapsed = currentMillis - state.resetStartMillis
        const newProgress = Math.min(100 * (elapsed/ RESET_MILLIS), 100)
        return { ...state, resetProgress: newProgress }
    }

    switch (action.type) {
        case SERVER_SWEEPER_ENTER:
            return enter(action as unknown as SweeperEnterFromServer)
        case SWEEPER_BEGIN:
            return { ...state, mode: VISIBLE }
        case SERVER_SWEEPER_MODIFY:
            return processModify(state, action.data)
        case SWEEPER_RESET_START:
            return processResetStart(state)
        case SWEEPER_RESET_STOP:
            return processResetStop(state)
        case SERVER_SWEEPER_BLOCK_USER:
            return blockUser(state, action.data)
        case SERVER_SWEEPER_SOLVED: {
            return {...state, hacked: true, minesLeft: 0}
        }
        default:
            return state
    }
}

interface SweeperEnterFromServer {
    data: SweeperGameState
}
const enter =  (action: SweeperEnterFromServer): SweeperUiState => {
    return {
        ...defaultState,
        strength: action.data.strength,
        hacked: action.data.hacked,
        blockedUserIds: action.data.blockedUserIds,
        minesLeft: action.data.minesLeft
    }
}

const processModify = (state: SweeperUiState, data: SweeperModifyData) => {
    if (data.action === SweeperModifyAction.FLAG || data.action === SweeperModifyAction.EXPLODE) {
        return { ...state, minesLeft: state.minesLeft! - 1}
    }
    if (data.action === SweeperModifyAction.CLEAR) {
        return { ...state, minesLeft: state.minesLeft! + 1}
    }
    return state
};


const processResetStart = (state: SweeperUiState): SweeperUiState => {
    if (state.resetState !== SweeperResetState.IDLE) {
        return state
    }
    return { ...state, resetState: SweeperResetState.IN_PROGRESS, resetProgress: 0, resetStartMillis: Date.now() }
}

const processResetStop = (state: SweeperUiState): SweeperUiState => {
    if (state.resetState !== SweeperResetState.IN_PROGRESS) {
        return state
    }
    return {...state, resetState: SweeperResetState.IDLE, resetProgress: 0}
}

interface BlockUserData {
    userId: string
}

const blockUser = (state: SweeperUiState, data: BlockUserData) => {
    return {...state, blockedUserIds: [...state.blockedUserIds, data.userId]}
}

