import {all, takeEvery} from 'redux-saga/effects'
import {
    AUTO_SCAN,
    PROBE_SCAN_NODE,

} from "./run/model/ScanActions";
import {DELETE_SCAN, RETRIEVE_USER_SCANS, SCAN_FOR_NAME} from "./home/HomeActions";
import {autoScanSaga, probeArriveSaga} from "./run/saga/ScanProbeSaga";
import {checkNavigateAwayFromScan, terminalSubmitCommandSaga} from "./run/saga/TerminalSagas";
import {
    deleteScanSaga,
    retrieveUserScansSaga,
    scanForNameSaga,
    } from "./run/saga/ScanSagas";
import {
    FINISH_HACKING_ICE,
} from "./run/model/HackActions";
import {SUBMIT_TERMINAL_COMMAND} from "./run/model/RunActions";
import {passwordIceFinish,  passwordIceSubmit} from "./run/ice/password/PasswordIceSagas";

import {NAVIGATE_PAGE} from "../common/menu/pageReducer";
import {ICE_PASSWORD_SUBMIT} from "./run/ice/password/PasswordIceHome";


const createHackerRootSaga = () => {

    function* allSagas() {


        // yield takeEvery(SERVER_TIME_SYNC, serverTimeSync);
        // yield takeEvery(SERVER_NOTIFICATION, serverNotificationSaga);
        // yield takeEvery(SERVER_DISCONNECT, serverDisconnectSaga);
        // yield takeEvery(SERVER_FORCE_DISCONNECT, serverForceDisconnectSaga);
        // yield takeEvery(SERVER_ERROR, serverErrorSaga);

        // yield takeEvery(NAVIGATE_PAGE, navigatePageSaga);
        // yield takeEvery(SERVER_HACKER_DC, serverUserDcSaga);

        yield takeEvery(RETRIEVE_USER_SCANS, retrieveUserScansSaga);
        yield takeEvery(SCAN_FOR_NAME, scanForNameSaga);
        yield takeEvery(DELETE_SCAN, deleteScanSaga);


        // yield takeEvery(ENTER_SCAN, enterScanSaga);
        yield takeEvery(NAVIGATE_PAGE, checkNavigateAwayFromScan);


        // yield takeEvery(SERVER_SITE_DISCOVERED, enterScanSaga);

        // yield takeEvery(SERVER_SCAN_FULL, serverScanFullSaga);
        yield takeEvery(SUBMIT_TERMINAL_COMMAND, terminalSubmitCommandSaga);

        // yield takeEvery(SERVER_UPDATE_NODE_STATUS, updateNodeStatusSaga);
        // yield takeEvery(SERVER_DISCOVER_NODES, discoverNodesSaga);

        // yield takeEvery(SERVER_PROBE_LAUNCH, serverProbeLaunchSaga);
        yield takeEvery(PROBE_SCAN_NODE, probeArriveSaga);
        yield takeEvery(AUTO_SCAN, autoScanSaga);

        // yield takeEvery(SERVER_HACKER_ENTER_SCAN, hackerEnterScanSaga);
        // yield takeEvery(SERVER_HACKER_LEAVE_SCAN, hackerLeaveScanSaga);

        // yield takeEvery(SERVER_HACKER_START_ATTACK, startAttackSaga);

        // yield takeEvery(SERVER_HACKER_MOVE_START, moveStartSaga);
        // yield takeEvery(SERVER_HACKER_MOVE_ARRIVE, serverMoveArriveSaga);

        // yield takeEvery(SERVER_HACKER_PROBE_LAYERS, serverHackerProbeLayersSaga);

        // yield takeEvery(SERVER_HACKER_PROBE_CONNECTIONS, serverHackerProbeConnectionsSaga);

        // yield takeEvery(SERVER_START_HACKING_ICE_PASSWORD, passwordIceStartHack);
        yield takeEvery(ICE_PASSWORD_SUBMIT, passwordIceSubmit);
        // yield takeEvery(SERVER_ICE_PASSWORD_UPDATE, serverPasswordIceUpdate);
        yield takeEvery(FINISH_HACKING_ICE, passwordIceFinish);

        // yield takeEvery(SERVER_NODE_HACKED, serverNodeHacked);

        // yield takeEvery(SERVER_FLASH_PATROLLER, serverFlashPatrollerSaga);
        // yield takeEvery(SERVER_START_TRACING_PATROLLER, serverStartPatrollerSaga);
        // yield takeEvery(SERVER_PATROLLER_MOVE, serverPatrollerMoveSaga);
        // yield takeEvery(SERVER_PATROLLER_HOOKS_HACKER, serverPatrollerHooksHackerSaga);
        // yield takeEvery(SERVER_PATROLLER_SNAPS_BACK_HACKER, serverPatrollerSnacksBackHackerSaga);
        // yield takeEvery(SERVER_PATROLLER_LOCKS_HACKER, serverPatrollerLocksHackerSaga);
        // yield takeEvery(SERVER_PATROLLER_REMOVE, serverRemovePatrollerSaga);
    }

    function* scanRootSaga() {
        yield all([
            allSagas(),
        ])
    }

    return scanRootSaga;
};


export default createHackerRootSaga