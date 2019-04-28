import { takeEvery, all } from 'redux-saga/effects'
import {REQUEST_SCAN_FULL, SERVER_SCAN_FULL} from "../ScanActions";
import createScanSagas from "./ScanFullSaga";
import {TERMINAL_SUBMIT} from "../../common/terminal/TerminalActions";

const createScanRootSaga = (stompClient, scanId, siteId) => {

    const [
        requestScanFullSaga, serverScanFullSaga, terminalSubmitSaga
    ] = createScanSagas(stompClient, scanId);

    function* allSagas() {
        yield takeEvery(REQUEST_SCAN_FULL, requestScanFullSaga);
        yield takeEvery(SERVER_SCAN_FULL, serverScanFullSaga);
        yield takeEvery(TERMINAL_SUBMIT, terminalSubmitSaga);
    }

    function* scanRootSaga() {
        yield all([
            allSagas(),
        ])
    }

    return scanRootSaga;
};

export default createScanRootSaga