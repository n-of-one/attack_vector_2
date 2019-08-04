import {select} from 'redux-saga/effects'
import webSocketConnection from "../../../../common/WebSocketConnection";


const getRunId = (state) => state.run.scan.runId;
const getPasswordIce = (state) => state.run.ice.password;


function* passwordIceSubmit(action) {
    const runId = yield select(getRunId);
    const ice = yield select(getPasswordIce);
    const payload = {serviceId: ice.serviceId, nodeId: ice.nodeId, runId: runId, password: action.password};
    webSocketConnection.send("/av/ice/password/submit", JSON.stringify(payload));
    yield
}

export {passwordIceSubmit}