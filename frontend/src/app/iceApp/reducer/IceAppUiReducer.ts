import {AnyAction} from "redux";
import {TERMINAL_UPDATE} from "../../../common/terminal/TerminalReducer";
import {serverTime} from "../../../common/server/ServerTime";
import {IceAppEnter, IceAppStateUpdate, SERVER_ENTER_ICE_APP, SERVER_ICE_APP_UPDATE} from "../IceAppServerActionProcessor";

export const ICE_PASSWORD_LOCK = "ICE_PASSWORD_LOCK";

export const UI_STATE_UNLOCKED = "UI_STATE_UNLOCKED"
export const UI_STATE_LOCKED = "UI_STATE_LOCKED"
export const UI_STATE_SUBMITTING = "UI_STATE_SUBMITTING"

export const SUBMIT_PASSWORD = "SUBMIT_PASSWORD"


export interface IceAppUi {
    state: "UI_STATE_UNLOCKED" | "UI_STATE_LOCKED" | "UI_STATE_SUBMITTING",
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

export const iceAppUiReducer = (state : IceAppUi = defaultUi, action: AnyAction): IceAppUi => {

    switch (action.type) {
        case TERMINAL_UPDATE: {
            return processTick(state)
        }

        case SERVER_ENTER_ICE_APP:
            return processEnterIce(action.data)
        case SERVER_ICE_APP_UPDATE:
            return processServerUpdate(action.data, state)
        case SUBMIT_PASSWORD:
            return {...state, state: UI_STATE_SUBMITTING}
        default:
            return state
    }
}

const processTick = (state: IceAppUi): IceAppUi => {
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

const processEnterIce = (serverStatus: IceAppEnter): IceAppUi => {
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

const processServerUpdate = (stateUpdate: IceAppStateUpdate, oldState: IceAppUi): IceAppUi => {
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
