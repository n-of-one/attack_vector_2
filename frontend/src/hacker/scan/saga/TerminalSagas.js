import webSocketConnection from "../../WebSocketConnection";
import {select} from 'redux-saga/effects'

const getScanId = (state) => state.scan.scan.id;


function* terminalSubmitSaga(action) {
    const scanId = yield select(getScanId);
    const payload = {scanId: scanId, command: action.command};
    let body = JSON.stringify(payload);
    webSocketConnection.send("/av/scan/terminal", body);
    yield
}

export {terminalSubmitSaga};