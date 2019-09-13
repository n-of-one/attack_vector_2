import {put} from 'redux-saga/effects'
import {notify, notify_fatal} from "../Notification";
import {TERMINAL_RECEIVE} from "../terminal/TerminalActions";
import serverTime from "../ServerTime";

export function* serverTimeSync(action) {
    serverTime.init(action.data);
    yield
}

export function* serverNotificationSaga(action) {
    notify(action.data);
    yield
}

export function* serverDisconnectSaga() {
    notify_fatal('Connection with server lost. Please refresh browser.');
    yield put({type: TERMINAL_RECEIVE, data: "[b warn]Connection with server lost. Please refresh browser."});
}

export function* serverForceDisconnectSaga(action) {
    notify(action.data);
    yield
}

export function* serverErrorSaga(action) {
    notify_fatal(action.data.message);
    yield
}
