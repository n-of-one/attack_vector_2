import { takeEvery, all } from 'redux-saga/effects'

const createScanSagas = (stompClient, siteId) => {


    function* allSagas() {
    }

    function* scanRootSaga() {
        yield all([
            allSagas(),
        ])
    }

    return scanRootSaga;
};

export default createScanSagas