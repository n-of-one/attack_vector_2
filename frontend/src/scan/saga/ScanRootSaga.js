import { takeEvery, all } from 'redux-saga/effects'
import {REQUEST_SCAN_FULL, SERVER_SCAN_FULL} from "../ScanActions";
import createScanSagas from "./ScanFullSaga";

const createScanRootSaga = (stompClient, scanId, siteId) => {

    const [
        requestScanFullSaga, serverScanFullSaga
    ] = createScanSagas(stompClient, scanId);

    function* allSagas() {
        yield takeEvery(REQUEST_SCAN_FULL, requestScanFullSaga);
        yield takeEvery(SERVER_SCAN_FULL, serverScanFullSaga);
    }

    function* scanRootSaga() {
        yield all([
            allSagas(),
        ])
    }

    return scanRootSaga;
};

export default createScanRootSaga