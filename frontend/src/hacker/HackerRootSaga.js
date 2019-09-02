import {all, takeEvery} from 'redux-saga/effects'
import {NAVIGATE_PAGE, SERVER_DISCONNECT, SERVER_ERROR, SERVER_FORCE_DISCONNECT, SERVER_NOTIFICATION} from "../common/enums/CommonActions";
import {
    AUTO_SCAN,
    PROBE_SCAN_NODE,
    SERVER_DISCOVER_NODES,
    SERVER_HACKER_ENTER_SCAN,
    SERVER_HACKER_LEAVE_SCAN,
    SERVER_PROBE_LAUNCH,
    SERVER_SCAN_FULL,
    SERVER_UPDATE_NODE_STATUS
} from "./run/model/ScanActions";
import {DELETE_SCAN, ENTER_SCAN, RETRIEVE_USER_SCANS, SCAN_FOR_NAME, SERVER_SITE_DISCOVERED} from "./home/HomeActions";
import {autoScanSaga, probeArriveSaga, serverProbeLaunchSaga} from "./run/saga/ScanProbeSaga";
import {discoverNodesSaga, serverNodeHacked, updateNodeStatusSaga} from "./run/saga/NodeSagas";
import {checkNavigateAwayFromScan, serverUserDcSaga, terminalSubmitCommandSaga} from "./run/saga/TerminalSagas";
import {SERVER_USER_DC} from "../common/terminal/TerminalActions";
import {
    deleteScanSaga,
    enterScanSaga,
    hackerEnterScanSaga,
    hackerLeaveScanSaga,
    navigatePageSaga,
    retrieveUserScansSaga,
    scanForNameSaga,
    serverScanFullSaga
} from "./run/saga/ScanSagas";
import {serverDisconnectSaga, serverErrorSaga, serverForceDisconnectSaga, serverNotificationSaga} from "../common/saga/ServerSagas";
import {
    hackerProbedConnectionsSaga,
    moveArriveSaga,
    moveStartSaga,
    probeLayersSaga,
    serverHackerProbeConnectionsSaga,
    serverHackerProbeLayersSaga,
    serverMoveArriveSaga,
    startAttackSaga
} from "./run/saga/HackSagas";
import {
    FINISH_HACKING_ICE,
    HACKER_MOVE_ARRIVE,
    HACKER_PROBED_CONNECTIONS,
    HACKER_PROBED_LAYERS,
    SERVER_HACKER_MOVE_ARRIVE,
    SERVER_HACKER_MOVE_START,
    SERVER_HACKER_PROBE_CONNECTIONS,
    SERVER_HACKER_PROBE_LAYERS,
    SERVER_HACKER_START_ATTACK,
    SERVER_NODE_HACKED
} from "./run/model/HackActions";
import {SUBMIT_TERMINAL_COMMAND} from "./run/model/RunActions";
import {ICE_PASSWORD_SUBMIT, SERVER_ICE_PASSWORD_UPDATE, SERVER_START_HACKING_ICE_PASSWORD} from "./run/ice/password/PasswordIceActions";
import {passwordIceFinish, passwordIceStartHack, passwordIceSubmit, serverPasswordIceUpdate} from "./run/ice/password/PasswordIceSagas";
import {SERVER_START_HACKING_ICE_TANGLE} from "./run/ice/tangle/TangleIceActions";
import {tangleIceStartHack} from "./run/ice/tangle/TangleSagas";

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
        yield takeEvery(SUBMIT_TERMINAL_COMMAND, terminalSubmitCommandSaga);

        yield takeEvery(SERVER_UPDATE_NODE_STATUS, updateNodeStatusSaga);
        yield takeEvery(SERVER_DISCOVER_NODES, discoverNodesSaga);

        yield takeEvery(SERVER_PROBE_LAUNCH, serverProbeLaunchSaga);
        yield takeEvery(PROBE_SCAN_NODE, probeArriveSaga);
        yield takeEvery(AUTO_SCAN, autoScanSaga);

        yield takeEvery(SERVER_HACKER_ENTER_SCAN, hackerEnterScanSaga);
        yield takeEvery(SERVER_HACKER_LEAVE_SCAN, hackerLeaveScanSaga);

        yield takeEvery(SERVER_HACKER_START_ATTACK, startAttackSaga);

        yield takeEvery(SERVER_HACKER_MOVE_START, moveStartSaga);
        yield takeEvery(HACKER_MOVE_ARRIVE, moveArriveSaga);
        yield takeEvery(SERVER_HACKER_MOVE_ARRIVE, serverMoveArriveSaga);

        yield takeEvery(SERVER_HACKER_PROBE_LAYERS, serverHackerProbeLayersSaga);
        yield takeEvery(HACKER_PROBED_LAYERS, probeLayersSaga);

        yield takeEvery(SERVER_HACKER_PROBE_CONNECTIONS, serverHackerProbeConnectionsSaga);
        yield takeEvery(HACKER_PROBED_CONNECTIONS, hackerProbedConnectionsSaga);

        yield takeEvery(SERVER_START_HACKING_ICE_PASSWORD, passwordIceStartHack);
        yield takeEvery(ICE_PASSWORD_SUBMIT, passwordIceSubmit);
        yield takeEvery(SERVER_ICE_PASSWORD_UPDATE, serverPasswordIceUpdate);
        yield takeEvery(FINISH_HACKING_ICE, passwordIceFinish);

        yield takeEvery(SERVER_NODE_HACKED, serverNodeHacked);

        yield takeEvery(SERVER_START_HACKING_ICE_TANGLE, tangleIceStartHack);


    }

    function* scanRootSaga() {
        yield all([
            allSagas(),
        ])
    }

    return scanRootSaga;
};


export default createHackerRootSaga