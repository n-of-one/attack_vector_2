import {select} from 'redux-saga/effects'
import runCanvas from "../component/RunCanvas";
import webSocketConnection from "../../../common/WebSocketConnection";

const getRunId = (state) => state.run.scan.runId;

function* startAttackSaga(action) {
    runCanvas.startAttack(action.data.userId, action.data.quick);
    yield
}

function* moveStartSaga(action) {
    runCanvas.moveStart(action.data.userId, action.data.nodeId);
    yield
}

function* moveArriveSaga(action) {
    const runId = yield select(getRunId);
    const payload = {nodeId: action.nodeId, runId: runId};
    webSocketConnection.send("/av/hack/moveArrive", JSON.stringify(payload));
    yield
}

function* serverMoveArriveSaga(action) {
    runCanvas.moveArrive(action.data.userId, action.data.nodeId);
    yield
}

function* serverHackerProbeLayersSaga(action) {
    runCanvas.hackerProbeLayersSaga(action.data.userId, action.data.nodeId);
    yield
}

function* probeLayersSaga(action) {
    const runId = yield select(getRunId);
    const payload = {nodeId: action.nodeId, runId: runId};
    webSocketConnection.send("/av/hack/probedLayers", JSON.stringify(payload));
    yield
}

function* serverHackerProbeConnectionsSaga(action) {
    runCanvas.hackerProbeConnections(action.data.userId, action.data.nodeId);
    yield
}

function* hackerProbedConnectionsSaga(action) {
    const runId = yield select(getRunId);
    const payload = {nodeId: action.nodeId, runId: runId};
    webSocketConnection.send("/av/hack/probedConnections", JSON.stringify(payload));
    yield
}

export {startAttackSaga,
    moveStartSaga, moveArriveSaga, serverMoveArriveSaga,
    serverHackerProbeLayersSaga, probeLayersSaga,
    serverHackerProbeConnectionsSaga, hackerProbedConnectionsSaga
}