import {notify, notify_fatal} from "../Notification";
import {TERMINAL_RECEIVE} from "../terminal/TerminalActions";

function* serverNotificationSaga(action) {
    notify(action.data);
    yield
}

function* serverDisconnectSaga(action) {
    notify_fatal('Connection with server lost. Please refresh browser.');
    this.dispatch({type: TERMINAL_RECEIVE, data: "[b warn]Connection with server lost. Please refresh browser."});
    yield
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