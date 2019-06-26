import scanCanvas from "../component/ScanCanvas"
import webSocketConnection from "../../../common/WebSocketConnection";
import {select} from 'redux-saga/effects'


function* serverProbeLaunchSaga(action) {
    scanCanvas.launchProbe(action.data);
    yield
}

const getRunId = (state) => state.run.scan.runId;

function* probeArriveSaga(action) {
    const runId = yield select(getRunId);
    const payload = {runId: runId, nodeId: action.nodeId, action: action.action};
    let body = JSON.stringify(payload);
    webSocketConnection.send("/av/scan/probeArrive", body);
    yield
}

function* autoScanSaga() {
    const runId = yield select(getRunId);
    webSocketConnection.send("/av/scan/autoScan", runId);
    yield
}

export {serverProbeLaunchSaga, probeArriveSaga, autoScanSaga};
