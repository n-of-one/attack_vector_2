import {SERVER_START_COUNTDOWN, SERVER_COMPLETE_COUNTDOWN} from "./CountdownActions";
import serverTime from "../../../common/ServerTime";
import {TERMINAL_TICK} from "../../../common/terminal/TerminalActions";
import {AnyAction} from "redux";


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
        case TERMINAL_TICK:
            return processTick(state);
        case SERVER_COMPLETE_COUNTDOWN:
            return processExpireTimer(state);
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
