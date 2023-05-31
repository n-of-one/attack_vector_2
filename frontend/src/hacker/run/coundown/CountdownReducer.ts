import {serverTime} from "../../../common/server/ServerTime";
import {AnyAction} from "redux";
import {TERMINAL_UPDATE} from "../../../common/terminal/TerminalReducer";
import {SERVER_HACKER_DC} from "../../server/RunServerActionProcessor";

export const SERVER_START_COUNTDOWN = "SERVER_START_COUNTDOWN";
export const SERVER_COMPLETE_COUNTDOWN = "SERVER_COMPLETE_COUNTDOWN";

export const SERVER_FLASH_PATROLLER = "SERVER_FLASH_PATROLLER";
export const SERVER_START_TRACING_PATROLLER = "SERVER_START_TRACING_PATROLLER";
export const SERVER_PATROLLER_MOVE = "SERVER_PATROLLER_MOVE";
export const SERVER_PATROLLER_LOCKS_HACKER = "SERVER_PATROLLER_LOCKS_HACKER";
export const SERVER_PATROLLER_REMOVE = "SERVER_PATROLLER_REMOVE";


export interface CountDownState {
    finishAt: string | null,    // "2019-08-26T15:38:40.9179757+02:00",
    secondsLeft: number | null,
    eventTriggered: boolean,
}

const defaultState: CountDownState = {
    finishAt: null,
    secondsLeft: null,     // 40
    eventTriggered: false
};

export const countdownReducer = (state: CountDownState = defaultState, action: AnyAction): CountDownState => {

    switch (action.type) {
        case SERVER_START_COUNTDOWN:
            return serverAlarmTrigger(state, action.data);
        case TERMINAL_UPDATE:
            return processTick(state);
        case SERVER_COMPLETE_COUNTDOWN:
            return processExpireTimer(state);
        case SERVER_HACKER_DC:
            return processHackerDisconnect(state);
        default:
            return state;
    }
}

interface CountdownStart {
    finishAt: string
}

const serverAlarmTrigger = (state: CountDownState, countDownStart: CountdownStart) => {
    const secondsLeft = serverTime.secondsLeft(countDownStart.finishAt);

    return {finishAt: countDownStart.finishAt, secondsLeft: secondsLeft, eventTriggered: false}
};

const processTick = (state: CountDownState) => {
    if (!state.finishAt || state.eventTriggered) {
        return state;
    }
    const secondsLeft = serverTime.secondsLeft(state.finishAt);

    return {finishAt: state.finishAt, secondsLeft: secondsLeft, eventTriggered: false};
};

const processExpireTimer = (state: CountDownState) => {
    return {
        ...state,
        secondsLeft: 0,
        eventTriggered: true
    };
};

const processHackerDisconnect = (state: CountDownState) => {
    return defaultState
}
