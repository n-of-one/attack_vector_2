import {select, put} from 'redux-saga/effects'
import {SERVER_COMPLETE_COUNTDOWN} from "./CountdownActions";
import runCanvas from "../component/RunCanvas";
import webSocketConnection from "../../../common/WebSocketConnection";

const getCountdown = (state) => state.run.countdown;
const getRunId = (state) => state.run.scan.runId;

export function* checkTimerSaga(action) {
    const countdown = yield select(getCountdown);

    // FIXME
    // if (countdown.finishAt != null && countdown.secondsLeft < 1 && !countdown.eventTriggered) {
    //     yield put({type: SERVER_COMPLETE_COUNTDOWN});
    // }
    yield
}

export function* serverCompleteCountdownSaga(action) {
    yield runCanvas.activateSniffer("node-6901-48c9")
}

export function* leashArriveHackerSaga(action) {
    const runId = yield select(getRunId);
    const payload = {nodeId: action.nodeId, runId: runId, };
    yield webSocketConnection.send("/av/hack/leashArriveHacker", JSON.stringify(payload));
}

export function* serverLeashLocksHackerSaga(action) {
    yield runCanvas.leashLocksHacker(action.data.hackerId);
}