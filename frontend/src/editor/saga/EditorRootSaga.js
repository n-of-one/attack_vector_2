import { takeEvery, all } from 'redux-saga/effects'
import {
    ADD_CONNECTION, DELETE_CONNECTIONS, DELETE_NODE, DRAG_DROP_END, EDIT_NETWORK_ID, EDIT_SERVICE_DATA, EDIT_SITE_DATA, MOVE_NODE, REQUEST_SITE_FULL,
    SERVER_ADD_CONNECTION, SERVER_ADD_NODE,
    SERVER_MOVE_NODE, SERVER_SITE_FULL, SNAP
} from "../EditorActions";
import {
    addConnectionSaga, deleteConnections, deleteNode, dropNodeSaga, moveNodeSaga, serverAddConnectionSaga,
    serverMoveNodeSaga, serverNodeAddedSaga, snap
} from "./NodeSagas";
import {editSiteDataSaga, requestSiteFullSaga, serverSiteFullSaga} from "./SiteDataSagas";
import {SERVER_DISCONNECT, SERVER_ERROR, SERVER_FORCE_DISCONNECT, SERVER_NOTIFICATION} from "../../common/enums/CommonActions";
import {serverDisconnectSaga, serverErrorSaga, serverForceDisconnectSaga, serverNotificationSaga} from "../../common/saga/ServerSagas";
import {editNetworkId, editServiceData} from "./ServiceSagas";

const createSagas = () => {


    function* allSagas() {
        yield takeEvery(SERVER_NOTIFICATION, serverNotificationSaga);
        yield takeEvery(SERVER_DISCONNECT, serverDisconnectSaga);
        yield takeEvery(SERVER_FORCE_DISCONNECT, serverForceDisconnectSaga);
        yield takeEvery(SERVER_ERROR, serverErrorSaga);

        yield takeEvery(REQUEST_SITE_FULL, requestSiteFullSaga);
        yield takeEvery(SERVER_SITE_FULL, serverSiteFullSaga);

        yield takeEvery(DRAG_DROP_END, dropNodeSaga);
        yield takeEvery(SERVER_ADD_NODE, serverNodeAddedSaga);

        yield takeEvery(MOVE_NODE, moveNodeSaga);
        yield takeEvery(SERVER_MOVE_NODE, serverMoveNodeSaga);

        yield takeEvery(ADD_CONNECTION, addConnectionSaga);
        yield takeEvery(SERVER_ADD_CONNECTION, serverAddConnectionSaga);

        yield takeEvery(EDIT_SITE_DATA, editSiteDataSaga);

        yield takeEvery(DELETE_CONNECTIONS, deleteConnections);
        yield takeEvery(DELETE_NODE, deleteNode);
        yield takeEvery(SNAP, snap);

        yield  takeEvery(EDIT_SERVICE_DATA, editServiceData);
        yield  takeEvery(EDIT_NETWORK_ID, editNetworkId);
    }

    function* editorRootSaga() {
        yield all([
            allSagas(),
        ])
    }

    return editorRootSaga;
};

export default createSagas