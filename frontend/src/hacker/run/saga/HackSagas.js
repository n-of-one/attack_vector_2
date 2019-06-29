import {select} from 'redux-saga/effects'
import scanCanvas from "../component/ScanCanvas";
import webSocketConnection from "../../../common/WebSocketConnection";

const getRunId = (state) => state.run.scan.runId;

function* startHackSaga(action) {
    scanCanvas.startHack(action.data.userId, action.data.quick);
    yield
}

function* moveStartSaga(action) {
    scanCanvas.moveStart(action.data.userId, action.data.nodeId);
    yield
}

function* moveArriveSaga(action) {
    const runId = yield select(getRunId);
    const payload = {nodeId: action.nodeId, runId: runId};
    webSocketConnection.send("/av/hack/moveArrive", JSON.stringify(payload));
    yield
}

function* serverMoveArriveSaga(action) {
    scanCanvas.moveArrive(action.data.userId, action.data.nodeId);
    yield
}

function* serverHackerProbeServicesSaga(action) {
    scanCanvas.hackerProbeServicesSaga(action.data.userId, action.data.nodeId);
    yield
}

function* probeServicesSaga(action) {
    const runId = yield select(getRunId);
    const payload = {nodeId: action.nodeId, runId: runId};
    webSocketConnection.send("/av/hack/probedServices", JSON.stringify(payload));
    yield
}

export {startHackSaga,
    moveStartSaga, moveArriveSaga, serverMoveArriveSaga,
    serverHackerProbeServicesSaga, probeServicesSaga,
}