import webSocketConnection from "../../../common/WebSocketConnection";
import {select, put} from 'redux-saga/effects'
import {NAVIGATE_PAGE} from "../../../common/enums/CommonActions";
import {HACKER_HOME, SCAN} from "../../HackerPages";

const getScanId = (state) => state.scan.scan.id;
const getCurrentPage = (state) => state.currentPage;


function* terminalSubmitSaga(action) {
    const scanId = yield select(getScanId);
    const payload = {scanId: scanId, command: action.command};
    let body = JSON.stringify(payload);
    webSocketConnection.send("/av/scan/terminal", body);
    yield
}

function* serverUserDcSaga() {
    const currentPage = yield select(getCurrentPage);
    yield put({ type: NAVIGATE_PAGE, to: HACKER_HOME, from: currentPage });

}

function* checkNavigateAwayFromScan(action) {
    const scanId = yield select(getScanId);
    if (action.from === SCAN && action.to !== SCAN) {
        webSocketConnection.send("/av/scan/leaveScan", scanId);
    }

}

export {terminalSubmitSaga, serverUserDcSaga, checkNavigateAwayFromScan};