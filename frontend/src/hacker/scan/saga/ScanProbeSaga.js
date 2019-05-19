import scanCanvas from "../component/ScanCanvas"
import webSocketConnection from "../../WebSocketConnection";
import {select} from 'redux-saga/effects'


function* serverProbeLaunchSaga(action) {
    scanCanvas.launchProbe(action.data);
    yield
}

const getScanId = (state) => state.scan.scan.id;

function* probeArriveSaga(action) {
    const scanId = yield select(getScanId);
    const payload = {scanId: scanId, nodeId: action.nodeId, action: action.action};
    let body = JSON.stringify(payload);
    webSocketConnection.send("/av/scan/probeArrive", body);
    yield
}

function* autoScanSaga() {
    const scanId = yield select(getScanId);
    webSocketConnection.send("/av/scan/autoScan", scanId);
    yield
}

export {serverProbeLaunchSaga, probeArriveSaga, autoScanSaga};
