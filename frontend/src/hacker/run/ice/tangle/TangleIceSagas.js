import tangleIceManager from "./TangleIceManager";
import {select} from 'redux-saga/effects'
import webSocketConnection from "../../../../common/WebSocketConnection";

const getRunId = (state) => state.run.scan.runId;
const getCurrentIce = (state) => state.run.ice.currentIce;

export function* tangleIceStartHack(action) {
    yield tangleIceManager.startHack(action.data);
}

export function* tangleIcePointMoved(action) {
    const runId = yield select(getRunId);
    const currentIce = yield select(getCurrentIce);

    const payload = {layerId: currentIce.layerId, runId: runId, id: action.id, x: action.x, y: action.y};
    webSocketConnection.send("/av/ice/tangle/moved", JSON.stringify(payload));
    yield
}

export function* tanglePointMoved(action) {
    const currentIce = yield select(getCurrentIce);
    if (currentIce.layerId === action.data.layerId) {
        tangleIceManager.moved(action.data);
    }
    yield
}