import { takeEvery, all } from 'redux-saga/effects'
import {
    ADD_CONNECTION,
    ADD_LAYER,
    DELETE_CONNECTIONS,
    DELETE_NODE,
    DRAG_DROP_END,
    EDIT_NETWORK_ID,
    EDIT_LAYER_DATA,
    EDIT_SITE_DATA,
    MOVE_NODE, REMOVE_LAYER,
    SERVER_ADD_CONNECTION,
    SERVER_ADD_NODE,
    SERVER_MOVE_NODE,
    SERVER_SITE_FULL,
    SNAP, SWAP_LAYERS
} from "./EditorActions";
import {
    addConnectionSaga, deleteConnections, deleteNode, dropNodeSaga, moveNodeSaga, serverAddConnectionSaga,
    serverMoveNodeSaga, serverNodeAddedSaga, snap
} from "./saga/NodeSagas";
import {serverSiteFullSaga} from "./saga/SiteDataSagas";
import {SERVER_DISCONNECT, SERVER_ERROR, SERVER_FORCE_DISCONNECT, SERVER_NOTIFICATION} from "../common/enums/CommonActions";
import {serverDisconnectSaga, serverErrorSaga, serverForceDisconnectSaga, serverNotificationSaga} from "../common/saga/ServerSagas";
import {addLayer, editNetworkId, editLayerData, removeLayer, swapLayers} from "./saga/LayerSagas";

const createSagas = () => {


    function* allSagas() {
        yield takeEvery(SERVER_NOTIFICATION, serverNotificationSaga);
        yield takeEvery(SERVER_DISCONNECT, serverDisconnectSaga);
        yield takeEvery(SERVER_FORCE_DISCONNECT, serverForceDisconnectSaga);
        yield takeEvery(SERVER_ERROR, serverErrorSaga);

        yield takeEvery(SERVER_SITE_FULL, serverSiteFullSaga);

        yield takeEvery(DRAG_DROP_END, dropNodeSaga);
        yield takeEvery(SERVER_ADD_NODE, serverNodeAddedSaga);

        yield takeEvery(MOVE_NODE, moveNodeSaga);
        yield takeEvery(SERVER_MOVE_NODE, serverMoveNodeSaga);

        yield takeEvery(ADD_CONNECTION, addConnectionSaga);
        yield takeEvery(SERVER_ADD_CONNECTION, serverAddConnectionSaga);

        yield takeEvery(DELETE_CONNECTIONS, deleteConnections);
        yield takeEvery(DELETE_NODE, deleteNode);
        yield takeEvery(SNAP, snap);

        yield takeEvery(EDIT_LAYER_DATA, editLayerData);
        yield takeEvery(EDIT_NETWORK_ID, editNetworkId);
        yield takeEvery(ADD_LAYER, addLayer);
        yield takeEvery(REMOVE_LAYER, removeLayer);
        yield takeEvery(SWAP_LAYERS, swapLayers);
    }

    function* editorRootSaga() {
        yield all([
            allSagas(),
        ])
    }

    return editorRootSaga;
};

export default createSagas