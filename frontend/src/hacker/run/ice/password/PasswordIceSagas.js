import {select, put} from 'redux-saga/effects'
import webSocketConnection from "../../../../common/WebSocketConnection";
import {notify} from "../../../../common/Notification";
import {TERMINAL_CLEAR, TERMINAL_RECEIVE} from "../../../../common/terminal/TerminalActions";
import {FINISH_HACKING_ICE} from "../../model/HackActions";
import {ICE_DISPLAY_TERMINAL_ID, ICE_TERMINAL_ID} from "../../../../common/terminal/ActiveTerminalIdReducer";
import {ICE_PASSWORD_UNLOCK} from "./PasswordIceActions";


const getRunId = (state) => state.run.scan.runId;
const getPasswordIce = (state) => state.run.ice.password;
const getCurrentIce = (state) => state.run.ice.currentIce;

const delay = (wait) => new Promise( res => setTimeout(res, 50 * wait));

export function* passwordIceStartHack(action) {
    yield put({type: TERMINAL_CLEAR, terminalId: ICE_TERMINAL_ID});
    yield * show(20, "↼ Connecting to ice, initiating attack.");
    yield * show(20, "↼ Scanning for weaknesses.");
    yield * show(20, "↼ .......................................................................................................................");
    yield * show(20, "↼ Found weak interface: static (non-rotating) password.");
    yield * show(20, "↼ Attempting brute force...");
    yield * show(20, "↺ Detected incremental time-out.");
    yield * show(20, "↺ Failed to sidestep incremental time-out.");
    yield * show(20, "↼ Suggested attack vectors: retrieve password, informed password guessing.");
    yield put({type: ICE_PASSWORD_UNLOCK});

}


export function* passwordIceSubmit(action) {
    const runId = yield select(getRunId);
    const ice = yield select(getPasswordIce);
    const payload = {serviceId: ice.status.serviceId, nodeId: ice.nodeId, runId: runId, password: action.password};
    webSocketConnection.send("/av/ice/password/submit", JSON.stringify(payload));
    yield
}

export function* serverPasswordIceUpdate(action) {
    const currentIce = yield select(getCurrentIce);
    if (currentIce.serviceId === action.data.status.serviceId) {
        if (action.data.hacked) {
            yield * processSuccess(action.data.message);
        } else {
            notify({type: "neutral", title: "Result", message: action.data.message})
            yield
        }
    }
}



export function* processSuccess(message) {
    notify({type: "ok", title: "Result", message: message});

    yield put({type: TERMINAL_RECEIVE, data: ""});
    yield put({type: TERMINAL_RECEIVE, data: "Password accepted", terminalId: ICE_TERMINAL_ID});
    yield delay(20);
    yield put({type: TERMINAL_RECEIVE, data: "ICE grants access.", terminalId: ICE_TERMINAL_ID});
    yield delay(40);
    yield put({type: FINISH_HACKING_ICE});
}


export function* show(wait, message) {
    yield put({type: TERMINAL_RECEIVE, terminalId: ICE_DISPLAY_TERMINAL_ID, data: message});
    yield delay(wait);
}
