import {takeEvery, all} from 'redux-saga/effects'
import {NAVIGATE_PAGE, SERVER_DISCONNECT, SERVER_ERROR, SERVER_FORCE_DISCONNECT, SERVER_NOTIFICATION} from "../common/enums/CommonActions";
import {AUTO_SCAN, PROBE_SCAN_NODE, SERVER_DISCOVER_NODES, SERVER_PROBE_LAUNCH, SERVER_SCAN_FULL, SERVER_UPDATE_NODE_STATUS} from "./scan/model/ScanActions";
import {ENTER_SCAN} from "./home/HomeActions";
import {autoScanSaga, probeArriveSaga, serverProbeLaunchSaga} from "./scan/saga/ScanProbeSaga";
import {discoverNodesSaga, updateNodeStatusSaga} from "./scan/saga/NodeSagas";
import {serverUserDcSaga, terminalSubmitSaga} from "./scan/saga/TerminalSagas";
import {SERVER_USER_DC, TERMINAL_SUBMIT} from "../common/terminal/TerminalActions";
import {enterScanSaga, navigatePageSaga, serverScanFullSaga} from "./scan/saga/ScanFullSaga";
import {serverDisconnectSaga, serverErrorSaga, serverForceDisconnectSaga, serverNotificationSaga} from "../common/saga/ServerSagas";

const createHackerRootSaga = () => {

    function* allSagas() {

        yield takeEvery(SERVER_NOTIFICATION, serverNotificationSaga);
        yield takeEvery(SERVER_DISCONNECT, serverDisconnectSaga);
        yield takeEvery(SERVER_FORCE_DISCONNECT, serverForceDisconnectSaga);
        yield takeEvery(SERVER_ERROR, serverErrorSaga);

        yield takeEvery(NAVIGATE_PAGE, navigatePageSaga);
        yield takeEvery(SERVER_USER_DC, serverUserDcSaga);
        yield takeEvery(ENTER_SCAN, enterScanSaga);
        yield takeEvery(SERVER_SCAN_FULL, serverScanFullSaga);
        yield takeEvery(TERMINAL_SUBMIT, terminalSubmitSaga);
        //
        yield takeEvery(SERVER_UPDATE_NODE_STATUS, updateNodeStatusSaga);
        yield takeEvery(SERVER_DISCOVER_NODES, discoverNodesSaga);
        //
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


export default createHackerRootSaga