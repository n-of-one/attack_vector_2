import {select} from 'redux-saga/effects'
import webSocketConnection from "../../../../common/WebSocketConnection";


const getRunId = (state) => state.run.scan.runId;
const getPasswordIce = (state) => state.run.ice.password;


function* passwordIceSubmit(action) {
    const runId = yield select(getRunId);
    const ice = yield select(getPasswordIce);

    const offset = new Date().getTimezoneOffset();
    const payload = {runId: runId, nodeId: ice.nodeId, serviceId: ice.serviceId, password: action.password, now: action.now, offset: offset};
    webSocketConnection.send("/av/ice/password/submit", JSON.stringify(payload));
    yield
}