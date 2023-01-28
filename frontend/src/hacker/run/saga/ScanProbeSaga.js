import runCanvas from "../component/RunCanvas"
import {webSocketConnection} from "../../../common/WebSocketConnection";
import {select} from 'redux-saga/effects'

const getRunId = (state) => state.run.scan.runId;

function* serverProbeLaunchSaga(action) {
    runCanvas.launchProbe(action.data);
    yield
}

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
