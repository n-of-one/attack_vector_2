import {put, select} from 'redux-saga/effects'
import {webSocketConnection} from "../../../../common/WebSocketConnection";
import {UNLOCKED} from "../IceUiState";
import {passwordIceManager} from "./PasswordIceManager";
import {ICE_PASSWORD_LOCK} from "./PasswordIceReducer";


const getRunId = (state) => state.run.scan.runId;
const getPasswordIce = (state) => state.run.ice.password;

// export function* passwordIceStartHack(action) {
//     yield passwordIceManager.passwordIceStartHack();
// }

// export function* serverPasswordIceUpdate(action) {
//     yield passwordIceManager.serverPasswordIceUpdate(action.data);
// }

export function* passwordIceFinish() {
    yield passwordIceManager.close();

}

export function* passwordIceSubmit(action) {
    const ice = yield select(getPasswordIce);
    if (ice.uiState !== UNLOCKED || ice.waitSeconds > 0) {
        return
    }
    if (action.password.trim().length === 0) {
        return
    }

    const runId = yield select(getRunId);
    const payload = {layerId: ice.layerId, nodeId: ice.nodeId, runId: runId, password: action.password};
    webSocketConnection.send("/av/ice/password/submit", JSON.stringify(payload));
    yield put({type: ICE_PASSWORD_LOCK});
}

