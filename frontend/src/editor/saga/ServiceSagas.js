import webSocketConnection from "../../common/WebSocketConnection";
import {select} from 'redux-saga/effects'
import editorCanvas from "../component/EditorCanvas";

const getSiteId = (state) => state.siteData.siteId;

function* editNetworkId(action) {
    yield toServer(action, "/av/editor/editNetworkId");
    editorCanvas.updateNetworkId(action);
    yield
}

function* editServiceData(action) {
    yield toServer(action, "/av/editor/editServiceData");
    yield
}

function* addService(action) {
    yield toServer(action, "/av/editor/addService");
    yield
}

function* removeService(action) {
    yield toServer(action, "/av/editor/removeService");
    yield
}

function* swapServiceLayer(action) {
    yield toServer(action, "/av/editor/swapServiceLayer");
    yield
}


function* toServer(action, path) {
    const siteId = yield select(getSiteId);
    const payload = {...action, type: undefined, siteId: siteId};
    webSocketConnection.sendObject(path, payload);
    yield
}

export { editServiceData, editNetworkId, addService, removeService, swapServiceLayer }
