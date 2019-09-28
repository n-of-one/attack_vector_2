
import {select} from 'redux-saga/effects'


const getAlarm = (state) => state.run.alarm;

export function* checkTimerSaga(action) {
    const alarm = yield select(getAlarm);


}