import {SERVER_START_COUNTDOWN, SERVER_COMPLETE_COUNTDOWN} from "./CountdownActions";
import serverTime from "../../../common/ServerTime";
import {TERMINAL_TICK} from "../../../common/terminal/TerminalActions";

const defaultState = {
    finishAt: null,              // "2019-08-26T15:38:40.9179757+02:00",
    secondsLeft: null,     // 40
    eventTriggered: false
};

const countdownReducer = (state = defaultState, action) => {

    switch (action.type) {
        case SERVER_START_COUNTDOWN:
            return serverAlarmTrigger(state, action.data.finishAt);
        case TERMINAL_TICK:
            return processTick(state);
        case SERVER_COMPLETE_COUNTDOWN:
            return processExpireTimer(state);
        default:
            return state;
    }
}

const serverAlarmTrigger = (state, finishAt) => {
    const secondsLeft = serverTime.secondsLeft(finishAt);

    return { finishAt: finishAt, secondsLeft: secondsLeft, eventTriggered: false }
};

const processTick = (state) => {
    if (!state.finishAt || state.eventTriggered) {
        return state;
    }
    const secondsLeft = serverTime.secondsLeft(state.finishAt);

    return {finishAt: state.finishAt, secondsLeft: secondsLeft, eventTriggered: false};
};

const processExpireTimer = (state) => {
    return {
        ...state,
        secondsLeft: 0,
        eventTriggered: true
    };
};

export default countdownReducer;
