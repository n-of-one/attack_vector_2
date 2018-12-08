import { takeEvery, all } from 'redux-saga/effects'
import {
    ADD_CONNECTION, DELETE_CONNECTIONS, DELETE_NODE, DRAG_DROP_END, EDIT_SITE_DATA, MOVE_NODE, REQUEST_SITE_FULL,
    SERVER_ADD_CONNECTION, SERVER_ADD_NODE,
    SERVER_MOVE_NODE, SERVER_SITE_FULL, SNAP
} from "../EditorActions";
import createNodeSagas from "./NodeSagas";
import createSiteDataSagas from "./SiteDataSagas";

const createSagas = (stompClient, siteId) => {

    let [
        requestSiteFullSaga, serverSiteFullSaga,
        dropNodeSaga, serverNodeAddedSaga,
        moveNodeSaga, serverMoveNodeSaga,
        addConnectionSaga, serverAddConnectionSaga,
        deleteConnections, deleteNode, snap,
    ] = createNodeSagas(stompClient, siteId);

    let [ editSiteDataSaga ] = createSiteDataSagas(stompClient, siteId);

    function* allSagas() {
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
    }

    function* editorRootSaga() {
        yield all([
            allSagas(),
        ])
    }

    return editorRootSaga;
};

export default createSagas