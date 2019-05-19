import { takeEvery, all, put, select } from 'redux-saga/effects'
// import {
//     AUTO_SCAN,
//     PROBE_SCAN_NODE,
//     ENTER_SCAN,
//     SERVER_DISCOVER_NODES,
//     SERVER_PROBE_LAUNCH,
//     SERVER_SCAN_FULL,
//     SERVER_UPDATE_NODE_STATUS
// } from "./scan/model/ScanActions";
// import createScanSagas from "./scan/saga/ScanFullSaga";
// import {TERMINAL_SUBMIT} from "../common/terminal/TerminalActions";
// import createProbeSagas from "./scan/saga/ScanProbeSaga";
import {NAVIGATE_PAGE, SERVER_NAVIGATE_PAGE} from "../common/enums/CommonActions";
import webSocketConnection from "./WebSocketConnection";
import {SCAN} from "./HackerPages";
import {ENTER_SCAN} from "./home/HomeActions";
import {AUTO_SCAN, PROBE_SCAN_NODE, SERVER_DISCOVER_NODES, SERVER_PROBE_LAUNCH, SERVER_SCAN_FULL, SERVER_UPDATE_NODE_STATUS} from "./scan/model/ScanActions";
import {autoScanSaga, probeArriveSaga, serverProbeLaunchSaga} from "./scan/saga/ScanProbeSaga";
import {discoverNodesSaga, updateNodeStatusSaga} from "./scan/saga/NodeSagas";
import scanCanvas from "./scan/component/ScanCanvas";
import {terminalSubmitSaga} from "./scan/saga/TerminalSagas";
import {TERMINAL_SUBMIT} from "../common/terminal/TerminalActions";
import {enterScanSaga, serverScanFullSaga} from "./scan/saga/ScanFullSaga";

const createHackerRootSaga = () => {

    // const [
    //     navigatePage, serverNavigatePage,
    //     enterScanSaga, serverScanFullSaga, terminalSubmitSaga,
    //     updateNodeStatusSaga, discoverNodesSaga
    // ] = createScanSagas(stompClient, scanId);
    //
    // const [serverProbeLaunchSaga, probeArriveSaga, autoScanSaga,
    // ] = createProbeSagas(stompClient, scanId);



    function* allSagas() {
        yield takeEvery(SERVER_NAVIGATE_PAGE, navigatePageSaga);
        yield takeEvery(NAVIGATE_PAGE, navigatePageSaga);
        // yield takeEvery(SERVER_NAVIGATE_PAGE, serverNavigatePage);
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

const getState = (state) => state;

function* navigatePageSaga(action) {
    const state = yield select(getState);

    // FIXME check current page, if moving away from scan, unsubscribe.
    // alert('navigage_page' );
    // doit(action, state);
    // window.location="/hacker";
    yield
}



function* serverScanFullSagaDebug(action) {
    scanCanvas.loadScan(action.data);
    yield
}

export default createHackerRootSaga