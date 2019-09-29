import {select, put} from 'redux-saga/effects'
import {} from 'redux-saga/effects'
import {SERVER_COMPLETE_COUNTDOWN} from "./CountdownActions";


const getAlarm = (state) => state.run.countdown;


export function* checkTimerSaga(action) {
    const alarm = yield select(getAlarm);

    if (alarm.secondsLeft < 1 && !alarm.eventTriggered) {
        yield put({type: SERVER_COMPLETE_COUNTDOWN});
    }
    yield
}