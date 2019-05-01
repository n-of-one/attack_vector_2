import { takeEvery, all } from 'redux-saga/effects'
import {PROBE_SCAN_NODE, REQUEST_SCAN_FULL, SERVER_PROBE_LAUNCH, SERVER_PROBE_SCAN_SUCCESS, SERVER_SCAN_FULL} from "../ScanActions";
import createScanSagas from "./ScanFullSaga";
import {TERMINAL_SUBMIT} from "../../common/terminal/TerminalActions";
import createProbeSagas from "./ScanProbeSaga";

const createScanRootSaga = (stompClient, scanId, siteId) => {

    const [
        requestScanFullSaga, serverScanFullSaga, terminalSubmitSaga
    ] = createScanSagas(stompClient, scanId);

    const [serverProbeActionSaga, probeArriveSaga, probeSuccessSaga,
    ] = createProbeSagas(stompClient, scanId);


    function* allSagas() {
        yield takeEvery(REQUEST_SCAN_FULL, requestScanFullSaga);
        yield takeEvery(SERVER_SCAN_FULL, serverScanFullSaga);
        yield takeEvery(TERMINAL_SUBMIT, terminalSubmitSaga);
        yield takeEvery(SERVER_PROBE_LAUNCH, serverProbeActionSaga);
        yield takeEvery(PROBE_SCAN_NODE, probeArriveSaga);
        yield takeEvery(SERVER_PROBE_SCAN_SUCCESS, probeSuccessSaga);
    }

    function* scanRootSaga() {
        yield all([
            allSagas(),
        ])
    }

    return scanRootSaga;
};

export default createScanRootSaga