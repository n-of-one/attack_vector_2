import { takeEvery, all } from 'redux-saga/effects'
import {
    SERVER_ADD_CONNECTION,
    SERVER_ADD_NODE,
    SERVER_MOVE_NODE,
    SERVER_SITE_FULL,
} from "./EditorActions";
import {
    serverAddConnectionSaga,
    serverMoveNodeSaga, serverNodeAddedSaga
} from "./saga/NodeSagas";
import {serverSiteFullSaga} from "./saga/SiteDataSagas";
import {SERVER_DISCONNECT, SERVER_ERROR, SERVER_FORCE_DISCONNECT, SERVER_NOTIFICATION} from "../common/enums/CommonActions";
import {serverDisconnectSaga, serverErrorSaga, serverForceDisconnectSaga, serverNotificationSaga} from "../common/saga/ServerSagas";

const createSagas = () => {


    function* allSagas() {
        yield takeEvery(SERVER_NOTIFICATION, serverNotificationSaga);
        yield takeEvery(SERVER_DISCONNECT, serverDisconnectSaga);
        yield takeEvery(SERVER_FORCE_DISCONNECT, serverForceDisconnectSaga);
        yield takeEvery(SERVER_ERROR, serverErrorSaga);

        yield takeEvery(SERVER_SITE_FULL, serverSiteFullSaga);
        yield takeEvery(SERVER_ADD_NODE, serverNodeAddedSaga);
        yield takeEvery(SERVER_MOVE_NODE, serverMoveNodeSaga);
        yield takeEvery(SERVER_ADD_CONNECTION, serverAddConnectionSaga);

    }

    function* editorRootSaga() {
        yield all([
            allSagas(),
        ])
    }

    return editorRootSaga;
};

export default createSagas