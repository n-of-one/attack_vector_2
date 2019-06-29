import webSocketConnection from "../../../common/WebSocketConnection";
import {select, put} from 'redux-saga/effects'
import {NAVIGATE_PAGE} from "../../../common/enums/CommonActions";
import {HACKER_HOME, SCAN} from "../../HackerPages";

const getRunId = (state) => state.run.scan.runId;
const getCurrentPage = (state) => state.currentPage;


function* terminalSubmitSaga(action) {
    const runId = yield select(getRunId);
    const payload = {runId: runId, command: action.command};
    let body = JSON.stringify(payload);
    webSocketConnection.send("/av/terminal/main", body);
    yield
}

function* serverUserDcSaga() {
    const currentPage = yield select(getCurrentPage);
    yield put({ type: NAVIGATE_PAGE, to: HACKER_HOME, from: currentPage });

}

function* checkNavigateAwayFromScan(action) {
    const runId = yield select(getRunId);
    if (action.from === SCAN && action.to !== SCAN) {
        webSocketConnection.send("/av/scan/leaveScan", runId);
    }

}

export {terminalSubmitSaga, serverUserDcSaga, checkNavigateAwayFromScan};