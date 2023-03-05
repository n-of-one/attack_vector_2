import {ICE_PASSWORD} from "../../../../common/enums/LayerTypes";
import {HIDDEN, LOCKED, UNLOCKED} from "../IceUiState";
import {FINISH_HACKING_ICE} from "../../model/HackActions";
import {serverTime} from "../../../../common/ServerTime";
import {AnyAction} from "redux";
import {TERMINAL_TICK} from "../../../../common/terminal/TerminalReducer";

export const SERVER_START_HACKING_ICE_PASSWORD = "SERVER_START_HACKING_ICE_PASSWORD";
export const SERVER_ICE_PASSWORD_UPDATE = "SERVER_ICE_PASSWORD_UPDATE";
export const ICE_PASSWORD_LOCK = "ICE_PASSWORD_LOCK";
export const ICE_PASSWORD_BEGIN = "ICE_PASSWORD_BEGIN";


export interface PasswordIceI {
    layerId: string,
    lockedUntil: string,
    attempts: string[],
    uiState: string,
    waitSeconds: number,
    hacked: boolean,
    hint?: string,
}

const defaultState = {
    layerId: "svc-7baa-4572-cde0",
    lockedUntil: "2019-08-26T15:38:40.9179757+02:00",
    attempts: ["zwaardvis", "secret"],
    uiState: "UNLOCKED",
    waitSeconds: 7,
    hacked: false,
    hint: "mother's name",
};

export const passwordIceReducer = (state : PasswordIceI= defaultState, action: AnyAction) => {
    return state

    // FIXME
    // if (!currentIce || currentIce.type !== ICE_PASSWORD) {
    //     return state;
    // }

    switch (action.type) {
        case TERMINAL_TICK: {
            return processTick(state);
        }

        case SERVER_START_HACKING_ICE_PASSWORD:
            return processStartHacking(action.data);
        case SERVER_ICE_PASSWORD_UPDATE:
            return processServerUpdate(action.data, state);
        case ICE_PASSWORD_BEGIN:
            return {...state, uiState: UNLOCKED};
        case ICE_PASSWORD_LOCK:
            return {...state, uiState: LOCKED};
        case FINISH_HACKING_ICE:
            return defaultState;
        default:
            return state;
    }
};

const processTick = (state: PasswordIceI) => {
    if (!state.waitSeconds || state.waitSeconds <= 0 ) {
        return state;
    }

    const waitSeconds = calculateWaitSeconds(state);
    return {...state, waitSeconds: waitSeconds, locked: false};
};

interface ServerStatus {
    message?: string,
    hacked: boolean,
    hint?: string,
    layerId: string,
    attempts: string[]
    lockedUntil: string
}

const processStartHacking = (serverStatus: ServerStatus): PasswordIceI => {
    const waitSeconds = calculateWaitSeconds(serverStatus);
    return {...serverStatus, waitSeconds: waitSeconds, uiState: HIDDEN };
};

const processServerUpdate = (serverStatus: ServerStatus, oldState: PasswordIceI) => {
    const waitSeconds = calculateWaitSeconds(serverStatus);
    return {...serverStatus, waitSeconds: waitSeconds, uiState: UNLOCKED};
};


const calculateWaitSeconds = (serverStatus: ServerStatus) => {
    return serverTime.secondsLeft(serverStatus.lockedUntil);
};
