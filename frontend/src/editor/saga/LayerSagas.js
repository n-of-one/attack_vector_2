import webSocketConnection from "../../common/WebSocketConnection";
import {select} from 'redux-saga/effects'
import editorCanvas from "../component/middle/middle/EditorCanvas";

const getSiteId = (state) => state.siteData.siteId;

function* editNetworkId(action) {
    yield toServer(action, "/av/editor/editNetworkId");
    editorCanvas.updateNetworkId(action);
    yield
}

function* editLayerData(action) {
    yield toServer(action, "/av/editor/editLayerData");
    yield
}

function* addLayer(action) {
    yield toServer(action, "/av/editor/addLayer");
    yield
}

function* removeLayer(action) {
    yield toServer(action, "/av/editor/removeLayer");
    yield
}

function* swapLayers(action) {
    yield toServer(action, "/av/editor/swapLayers");
    yield
}


function* toServer(action, path) {
    const siteId = yield select(getSiteId);
    const payload = {...action, type: undefined, siteId: siteId};
    webSocketConnection.sendObject(path, payload);
    yield
}

export { editLayerData, editNetworkId, addLayer, removeLayer, swapLayers }
