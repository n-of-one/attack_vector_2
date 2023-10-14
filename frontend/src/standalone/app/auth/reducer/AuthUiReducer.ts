import {AnyAction} from "redux";
import {TICK} from "../../../../common/terminal/TerminalReducer";
import {serverTime} from "../../../../common/server/ServerTime";
import {AuthEnter, AuthStateUpdate, SERVER_AUTH_ENTER, SERVER_AUTH_PASSWORD_CORRECT, SERVER_AUTH_UPDATE} from "../AuthServerActionProcessor";

export const ICE_PASSWORD_LOCK = "ICE_PASSWORD_LOCK";

export const UI_STATE_UNLOCKED = "UI_STATE_UNLOCKED"
export const UI_STATE_LOCKED = "UI_STATE_LOCKED"
export const UI_STATE_SUBMITTING = "UI_STATE_SUBMITTING"
export const UI_STATE_PASSWORD_CORRECT = "UI_STATE_PASSWORD_CORRECT"

export const SUBMIT_PASSWORD = "SUBMIT_PASSWORD"


export interface AuthAppUi {
    state: "UI_STATE_UNLOCKED" | "UI_STATE_LOCKED" | "UI_STATE_SUBMITTING" | "UI_STATE_PASSWORD_CORRECT",
    waitSeconds: number,
    lockedUntil: string  // "2019-08-26T15:38:40.9179757+02:00"
    attempts: string[],
    hacked: boolean,
    showHint: boolean,
}

export const defaultUi = {
    state: UI_STATE_LOCKED as "UI_STATE_LOCKED",
    waitSeconds: 0,
    lockedUntil: "2019-08-26T15:38:40.9179757+02:00",
    type: "unknown",
    attempts:  [],
    hacked: false,
    showHint: false,
}

export const authUiReducer = (state : AuthAppUi = defaultUi, action: AnyAction): AuthAppUi => {

    switch (action.type) {
        case TICK: {
            return processTick(state)
        }

        case SERVER_AUTH_ENTER:
            return processEnterIce(action.data)
        case SERVER_AUTH_UPDATE:
            return processServerUpdate(action.data, state)
        case SUBMIT_PASSWORD:
            return {...state, state: UI_STATE_SUBMITTING}
        case SERVER_AUTH_PASSWORD_CORRECT:
        return { ...state, state: UI_STATE_PASSWORD_CORRECT}
        default:
            return state
    }
}

const processTick = (state: AuthAppUi): AuthAppUi => {
    if (state.waitSeconds <= 0 ) {
        return state;
    }

    const waitSeconds = calculateWaitSeconds(state)
    if (waitSeconds > 0) {
        return {...state, waitSeconds: waitSeconds}
    }
    else {
        return {...state, waitSeconds: 0, state: UI_STATE_UNLOCKED}
    }
}

const processEnterIce = (serverStatus: AuthEnter): AuthAppUi => {
    const waitSeconds = calculateWaitSeconds(serverStatus);
    return {
        state: (waitSeconds > 0) ? UI_STATE_LOCKED : UI_STATE_UNLOCKED,
        waitSeconds: waitSeconds,
        lockedUntil: serverStatus.lockedUntil,
        attempts: serverStatus.attempts,
        hacked: serverStatus.hacked,
        showHint: serverStatus.showHint
    }
}

const processServerUpdate = (stateUpdate: AuthStateUpdate, oldState: AuthAppUi): AuthAppUi => {
    const waitSeconds = calculateWaitSeconds(stateUpdate);
    return {...oldState,
        waitSeconds: waitSeconds,
        lockedUntil: stateUpdate.lockedUntil,
        state: (waitSeconds > 0) ? UI_STATE_LOCKED : UI_STATE_UNLOCKED,
        hacked: stateUpdate.hacked,
        attempts: stateUpdate.attempts,
        showHint: stateUpdate.showHint
    }
}

interface WithLockedUntil { lockedUntil: string}
const calculateWaitSeconds = (serverStatus: WithLockedUntil) => {
    return serverTime.secondsLeft(serverStatus.lockedUntil);
}
