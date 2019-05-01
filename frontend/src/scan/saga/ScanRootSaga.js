import { takeEvery, all } from 'redux-saga/effects'
import {
    AUTO_SCAN,
    PROBE_SCAN_NODE,
    REQUEST_SCAN_FULL,
    SERVER_DISCOVER_NODES,
    SERVER_PROBE_LAUNCH,
    SERVER_SCAN_FULL,
    SERVER_UPDATE_NODE_STATUS
} from "../ScanActions";
import createScanSagas from "./ScanFullSaga";
import {TERMINAL_SUBMIT} from "../../common/terminal/TerminalActions";
import createProbeSagas from "./ScanProbeSaga";
import {NAVIGATE_PAGE} from "../../common/CommonActions";

const createScanRootSaga = (stompClient, scanId, siteId) => {

    const [
        navigatePage,
        requestScanFullSaga, serverScanFullSaga, terminalSubmitSaga,
        updateNodeStatusSaga, discoverNodesSaga
    ] = createScanSagas(stompClient, scanId);

    const [serverProbeLaunchSaga, probeArriveSaga, autoScanSaga,
    ] = createProbeSagas(stompClient, scanId);


    function* allSagas() {
        yield takeEvery(NAVIGATE_PAGE, navigatePage);
        yield takeEvery(REQUEST_SCAN_FULL, requestScanFullSaga);
        yield takeEvery(SERVER_SCAN_FULL, serverScanFullSaga);
        yield takeEvery(TERMINAL_SUBMIT, terminalSubmitSaga);

        yield takeEvery(SERVER_UPDATE_NODE_STATUS, updateNodeStatusSaga);
        yield takeEvery(SERVER_DISCOVER_NODES, discoverNodesSaga);

        yield takeEvery(SERVER_PROBE_LAUNCH, serverProbeLaunchSaga);
        yield takeEvery(PROBE_SCAN_NODE, probeArriveSaga);
        yield takeEvery(AUTO_SCAN, autoScanSaga);
    }

    function* scanRootSaga() {
        yield all([
            allSagas(),
        ])
    }

    return scanRootSaga;
};

export default createScanRootSaga