import {all, takeEvery} from 'redux-saga/effects'
import {
    AUTO_SCAN,
    PROBE_SCAN_NODE,

} from "./run/model/ScanActions";
import {autoScanSaga, probeArriveSaga} from "./run/saga/ScanProbeSaga";
import {terminalSubmitCommandSaga} from "./run/saga/TerminalSagas";
import {
    FINISH_HACKING_ICE,
} from "./run/model/HackActions";
import {SUBMIT_TERMINAL_COMMAND} from "./run/model/RunActions";
import {passwordIceFinish,  passwordIceSubmit} from "./run/ice/password/PasswordIceSagas";

import {ICE_PASSWORD_SUBMIT} from "./run/ice/password/PasswordIceHome";


const createHackerRootSaga = () => {

    function* allSagas() {

        yield takeEvery(SUBMIT_TERMINAL_COMMAND, terminalSubmitCommandSaga);

        yield takeEvery(PROBE_SCAN_NODE, probeArriveSaga);
        yield takeEvery(AUTO_SCAN, autoScanSaga);

        yield takeEvery(ICE_PASSWORD_SUBMIT, passwordIceSubmit);
        yield takeEvery(FINISH_HACKING_ICE, passwordIceFinish);

    }

    function* scanRootSaga() {
        yield all([
            allSagas(),
        ])
    }

    return scanRootSaga;
};


export default createHackerRootSaga