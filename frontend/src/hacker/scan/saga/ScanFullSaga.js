import scanCanvas from "../component/ScanCanvas"
import webSocketConnection from "../../WebSocketConnection";
import {NAVIGATE_PAGE} from "../../../common/enums/CommonActions";
import {SCAN} from "../../HackerPages";
import { select, put } from 'redux-saga/effects'
import {SERVER_SCAN_FULL, WAITING_FOR_SCAN_IGNORE_LIST} from "../model/ScanActions";
import {TERMINAL_CLEAR} from "../../../common/terminal/TerminalActions";

const getState = (state) => state;
const getCurrentPage = (state) => state.currentPage;

function* enterScanSaga(action) {
    const {scanId, siteId} = action;
    const currentPage = yield select(getCurrentPage);

    webSocketConnection.waitFor(SERVER_SCAN_FULL, WAITING_FOR_SCAN_IGNORE_LIST);
    webSocketConnection.subscribeForScan(scanId, siteId);
    scanCanvas.reset();
    yield put({ type: TERMINAL_CLEAR, terminalId: "main"});
    yield put({ type: NAVIGATE_PAGE, to: SCAN, from: currentPage });
    webSocketConnection.send("/av/scan/enterScan", scanId);
    yield
}

function* serverScanFullSaga(action) {
    scanCanvas.loadScan(action.data);
    yield
}

function* navigatePageSaga(action) {
    if (action.from === SCAN && action.to !== SCAN) {
        const state = yield select(getState);
        debug(state);
    }
    yield
}

const debug= (state)=> {
    const debug = 1;
    const scanId = state.scan.scan.id;
    const siteId = state.scan.site.id;
    webSocketConnection.unsubscribeForScan(scanId, siteId);
};


export { enterScanSaga, serverScanFullSaga, navigatePageSaga };
