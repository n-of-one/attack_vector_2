import {put} from 'redux-saga/effects'
import {notify, notify_fatal} from "../Notification";
import {TERMINAL_RECEIVE} from "../terminal/TerminalActions";

function* serverNotificationSaga(action) {
    notify(action.data);
    yield
}

function* serverDisconnectSaga() {
    notify_fatal('Connection with server lost. Please refresh browser.');
    yield put({type: TERMINAL_RECEIVE, data: "[b warn]Connection with server lost. Please refresh browser."});
}

function* serverForceDisconnectSaga(action) {
    notify(action.data);
    yield
}

function* serverErrorSaga(action) {
    notify_fatal(action.data.message);
    yield
}

export {serverNotificationSaga, serverDisconnectSaga, serverForceDisconnectSaga, serverErrorSaga}