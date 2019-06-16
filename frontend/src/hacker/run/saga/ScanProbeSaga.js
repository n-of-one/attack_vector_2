import scanCanvas from "../component/ScanCanvas"
import webSocketConnection from "../../../common/WebSocketConnection";
import {select} from 'redux-saga/effects'


function* serverProbeLaunchSaga(action) {
    scanCanvas.launchProbe(action.data);
    yield
}

const getScanId = (state) => state.run.scan.id;

function* probeArriveSaga(action) {
    const runId = yield select(getScanId);
    const payload = {runId: runId, nodeId: action.nodeId, action: action.action};
    let body = JSON.stringify(payload);
    webSocketConnection.send("/av/scan/probeArrive", body);
    yield
}

function* autoScanSaga() {
    const runId = yield select(getScanId);
    webSocketConnection.send("/av/scan/autoScan", runId);
    yield
}

export {serverProbeLaunchSaga, probeArriveSaga, autoScanSaga};
