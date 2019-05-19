import webSocketConnection from "../../WebSocketConnection";
import {select, put} from 'redux-saga/effects'
import {NAVIGATE_PAGE} from "../../../common/enums/CommonActions";
import {HACKER_HOME} from "../../HackerPages";

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

export {terminalSubmitSaga, serverUserDcSaga};