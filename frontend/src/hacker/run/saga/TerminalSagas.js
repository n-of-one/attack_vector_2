import {webSocketConnection} from "../../../common/WebSocketConnection";
import {select} from 'redux-saga/effects'
import {SCAN} from "../../../common/menu/pageReducer";

const getRunId = (state) => state.run.scan.runId;


function* terminalSubmitCommandSaga(action) {
    const runId = yield select(getRunId);
    const payload = {runId: runId, command: action.command};
    let body = JSON.stringify(payload);
    webSocketConnection.send("/av/terminal/main", body);
    yield
}

// function* serverUserDcSaga() {
//     const currentPage = yield select(getCurrentPage);
//     yield put({ type: NAVIGATE_PAGE, to: HACKER_HOME, from: currentPage });
//
// }

function* checkNavigateAwayFromScan(action) {
    const runId = yield select(getRunId);
    if (action.from === SCAN && action.to !== SCAN) {
        webSocketConnection.send("/av/scan/leaveScan", runId);
    }

}

export {terminalSubmitCommandSaga,  checkNavigateAwayFromScan};