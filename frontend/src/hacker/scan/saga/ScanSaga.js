import scanCanvas from "../component/ScanCanvas"
import webSocketConnection from "../../../common/WebSocketConnection";
import {NAVIGATE_PAGE} from "../../../common/enums/CommonActions";
import {SCAN} from "../../HackerPages";
import { select, put } from 'redux-saga/effects'
import {SERVER_SCAN_FULL, WAITING_FOR_SCAN_IGNORE_LIST} from "../model/ScanActions";
import {TERMINAL_CLEAR} from "../../../common/terminal/TerminalActions";
import terminalManager from "../../../common/terminal/TerminalManager";

const getCurrentPage = (state) => state.currentPage;


function* retrieveUserScansSaga() {
    webSocketConnection.send("/av/scan/scansOfPlayer", "");
    yield
}

function* scanForNameSaga(action) {
    webSocketConnection.send("/av/scan/scanForName", action.siteName);
    yield
}

function* enterScanSaga(action) {
    const {scanId, siteId} = action.data;
    const currentPage = yield select(getCurrentPage);

    webSocketConnection.waitFor(SERVER_SCAN_FULL, WAITING_FOR_SCAN_IGNORE_LIST);
    webSocketConnection.subscribeForScan(scanId, siteId);
    scanCanvas.reset();
    yield put({ type: TERMINAL_CLEAR, terminalId: "main"});
    yield put({ type: NAVIGATE_PAGE, to: SCAN, from: currentPage });
    webSocketConnection.send("/av/scan/enterScan", scanId);
    terminalManager.start();
    yield
}

function* serverScanFullSaga(action) {
    scanCanvas.loadScan(action.data);
    yield
}

function* navigatePageSaga(action) {
    if (action.from === SCAN && action.to !== SCAN) {
        webSocketConnection.unsubscribe();
        terminalManager.stop();
    }
    yield
}

function* hackerEnterScanSaga(action) {
    scanCanvas.hackerEnter(action.data);
    yield
}

function* hackerLeaveScanSaga(action) {
    scanCanvas.hackerLeave(action.data);
    yield
}

export { enterScanSaga, serverScanFullSaga, navigatePageSaga, retrieveUserScansSaga, scanForNameSaga,
    hackerEnterScanSaga, hackerLeaveScanSaga };
