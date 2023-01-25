import runCanvas from "../component/RunCanvas"
import webSocketConnection from "../../../common/WebSocketConnection";
import { select, put } from 'redux-saga/effects'
import {HIDE_NODE_INFO, SERVER_SCAN_FULL, WAITING_FOR_SCAN_IGNORE_LIST} from "../model/ScanActions";
import {TERMINAL_CLEAR} from "../../../common/terminal/TerminalActions";
import terminalManager from "../../../common/terminal/TerminalManager";
import {NAVIGATE_PAGE, SCAN} from "../../../common/menu/pageReducer";

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
    const {runId, siteId} = action.data;
    const currentPage = yield select(getCurrentPage);

    webSocketConnection.waitFor(SERVER_SCAN_FULL, WAITING_FOR_SCAN_IGNORE_LIST);
    webSocketConnection.subscribeForRun(runId, siteId);
    runCanvas.reset();
    yield put({ type: HIDE_NODE_INFO });
    yield put({ type: TERMINAL_CLEAR, terminalId: "main"});
    yield put({ type: NAVIGATE_PAGE, to: SCAN, from: currentPage });
    webSocketConnection.send("/av/scan/enterScan", runId);
    terminalManager.start();
    yield
}

function* deleteScanSaga(action) {
    webSocketConnection.send("/av/scan/deleteScan", action.runId);
    yield
}

function* serverScanFullSaga(action) {
    runCanvas.loadScan(action.data);
    yield
}

function* navigatePageSaga(action) {
    if (action.from === SCAN && action.to !== SCAN) {
        webSocketConnection.unsubscribe();
        terminalManager.stop();
        runCanvas.stop();
    }
    yield
}

function* hackerEnterScanSaga(action) {
    runCanvas.hackerEnter(action.data);
    yield
}

function* hackerLeaveScanSaga(action) {
    runCanvas.hackerLeave(action.data);
    yield
}

export { enterScanSaga, deleteScanSaga, serverScanFullSaga, navigatePageSaga, retrieveUserScansSaga, scanForNameSaga,
    hackerEnterScanSaga, hackerLeaveScanSaga };
