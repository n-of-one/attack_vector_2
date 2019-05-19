import scanCanvas from "../component/ScanCanvas"
import webSocketConnection from "../../WebSocketConnection";
import {NAVIGATE_PAGE} from "../../../common/enums/CommonActions";
import {SCAN} from "../../HackerPages";
import { put } from 'redux-saga/effects'

function* enterScanSaga(action) {
    const {scanId, siteId} = action;
    webSocketConnection.subScribeForScan(scanId, siteId);
    webSocketConnection.send("/av/scan/enterScan", scanId);
    yield put({ type: NAVIGATE_PAGE, target: SCAN });
    yield
}

function* serverScanFullSaga(action) {
    scanCanvas.loadScan(action.data);
    alert('serverScanFullSaga');
    yield
}

export { enterScanSaga, serverScanFullSaga };
