import {SERVER_TRIGGER_TIMER} from "./AlarmActions";
import serverTime from "../../../common/ServerTime";
import {TERMINAL_TICK} from "../../../common/terminal/TerminalActions";

const defaultState = {
    alarmAt: null,              // "2019-08-26T15:38:40.9179757+02:00",
    secondsUntilAlarm: null,     // 40
    alarmTriggered: false
};



export default (state = defaultState, action) => {

    switch (action.type) {
        case SERVER_TRIGGER_TIMER:
            return serverAlarmTrigger(state, action.data.alarm);
        case TERMINAL_TICK: {
            return processTick(state);
        }
        default:
            return state;
    }
}

const serverAlarmTrigger = (state, alarm) => {
    const alarmAt = alarm;
    const secondsUntilAlarm = serverTime.secondsLeft(alarm);

    return { ...state, alarmAt, secondsUntilAlarm, }
};

const processTick = (state) => {
    if (!state.alarmAt || state.alarmTriggered) {
        return state;
    }
    const secondsUntilAlarm = serverTime.secondsLeft(state.alarmAt);
    if (secondsUntilAlarm === 0) {
        return {
            ...state, secondsUntilAlarm: 0, alarmTriggered: true
        }
    }

    return {alarmAt: state.alarmAt, secondsUntilAlarm: secondsUntilAlarm};
};
