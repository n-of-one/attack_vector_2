import {select, put} from 'redux-saga/effects'
import {} from 'redux-saga/effects'
import {SERVER_EXPIRE_TIMER} from "./AlarmActions";


const getAlarm = (state) => state.run.alarm;


export function* checkTimerSaga(action) {
    const alarm = yield select(getAlarm);

    if (alarm.secondsUntilAlarm < 1 && !alarm.alarmTriggered) {
        yield put({type: SERVER_EXPIRE_TIMER});
    }
    yield
}