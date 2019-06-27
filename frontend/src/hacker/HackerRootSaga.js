import {takeEvery, all} from 'redux-saga/effects'
import {NAVIGATE_PAGE, SERVER_DISCONNECT, SERVER_ERROR, SERVER_FORCE_DISCONNECT, SERVER_NOTIFICATION} from "../common/enums/CommonActions";
import {
    AUTO_SCAN,
    PROBE_SCAN_NODE,
    SERVER_DISCOVER_NODES,
    SERVER_HACKER_ENTER_SCAN, SERVER_HACKER_LEAVE_SCAN,
    SERVER_PROBE_LAUNCH,
    SERVER_SCAN_FULL,
    SERVER_UPDATE_NODE_STATUS
} from "./run/model/ScanActions";
import {DELETE_SCAN, ENTER_SCAN, RETRIEVE_USER_SCANS, SCAN_FOR_NAME, SERVER_SITE_DISCOVERED} from "./home/HomeActions";
import {autoScanSaga, probeArriveSaga, serverProbeLaunchSaga} from "./run/saga/ScanProbeSaga";
import {discoverNodesSaga, updateNodeStatusSaga} from "./run/saga/NodeSagas";
import {checkNavigateAwayFromScan, serverUserDcSaga, terminalSubmitSaga} from "./run/saga/TerminalSagas";
import {SERVER_USER_DC, TERMINAL_SUBMIT} from "../common/terminal/TerminalActions";
import {
    enterScanSaga,
    navigatePageSaga,
    retrieveUserScansSaga,
    serverScanFullSaga,
    scanForNameSaga,
    hackerEnterScanSaga,
    hackerLeaveScanSaga, deleteScanSaga
} from "./run/saga/ScanSagas";
import {serverDisconnectSaga, serverErrorSaga, serverForceDisconnectSaga, serverNotificationSaga} from "../common/saga/ServerSagas";
import {enterRunSaga, moveArriveSaga, moveStartSaga, serverMoveArriveSaga} from "./run/saga/RunSagas";
import {HACKER_MOVE_ARRIVE, SERVER_HACKER_ENTER_RUN, SERVER_HACKER_MOVE_ARRIVE, SERVER_HACKER_MOVE_START} from "./run/model/RunActions";

const createHackerRootSaga = () => {

    function* allSagas() {

        yield takeEvery(SERVER_NOTIFICATION, serverNotificationSaga);
        yield takeEvery(SERVER_DISCONNECT, serverDisconnectSaga);
        yield takeEvery(SERVER_FORCE_DISCONNECT, serverForceDisconnectSaga);
        yield takeEvery(SERVER_ERROR, serverErrorSaga);

        yield takeEvery(NAVIGATE_PAGE, navigatePageSaga);
        yield takeEvery(SERVER_USER_DC, serverUserDcSaga);

        yield takeEvery(RETRIEVE_USER_SCANS, retrieveUserScansSaga);
        yield takeEvery(SCAN_FOR_NAME, scanForNameSaga);
        yield takeEvery(DELETE_SCAN, deleteScanSaga);


        yield takeEvery(ENTER_SCAN, enterScanSaga);
        yield takeEvery(NAVIGATE_PAGE, checkNavigateAwayFromScan);


        yield takeEvery(SERVER_SITE_DISCOVERED, enterScanSaga);

        yield takeEvery(SERVER_SCAN_FULL, serverScanFullSaga);
        yield takeEvery(TERMINAL_SUBMIT, terminalSubmitSaga);
        //
        yield takeEvery(SERVER_UPDATE_NODE_STATUS, updateNodeStatusSaga);
        yield takeEvery(SERVER_DISCOVER_NODES, discoverNodesSaga);
        //
        yield takeEvery(SERVER_PROBE_LAUNCH, serverProbeLaunchSaga);
        yield takeEvery(PROBE_SCAN_NODE, probeArriveSaga);
        yield takeEvery(AUTO_SCAN, autoScanSaga);

        yield takeEvery(SERVER_HACKER_ENTER_SCAN, hackerEnterScanSaga);
        yield takeEvery(SERVER_HACKER_LEAVE_SCAN, hackerLeaveScanSaga);

        yield takeEvery(SERVER_HACKER_ENTER_RUN, enterRunSaga);
        yield takeEvery(SERVER_HACKER_MOVE_START, moveStartSaga);
        yield takeEvery(HACKER_MOVE_ARRIVE, moveArriveSaga);
        yield takeEvery(SERVER_HACKER_MOVE_ARRIVE, serverMoveArriveSaga);
    }

    function* scanRootSaga() {
        yield all([
            allSagas(),
        ])
    }

    return scanRootSaga;
};


export default createHackerRootSaga