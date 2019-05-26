import webSocketConnection from "../../common/WebSocketConnection";
import {select} from 'redux-saga/effects'
import editorCanvas from "../component/EditorCanvas";

const getSiteId = (state) => state.siteData.id;

// nodeId: this.node.id, serviceId: this.service.id, key: key, value: value

function* editNetworkId(action) {
    const siteId = yield select(getSiteId);
    const payload = {...action, type: undefined, siteId: siteId};
    webSocketConnection.sendObject("/av/editor/editNetworkId", payload);
    editorCanvas.updateNetworkId(action);
    yield
}

function* editServiceData(action) {
    const siteId = yield select(getSiteId);
    const payload = {...action, type: undefined, siteId: siteId};
    webSocketConnection.sendObject("/av/editor/editServiceData", payload);
    yield
}

function* addService(action) {
    const siteId = yield select(getSiteId);
    const payload = {...action, type: undefined, siteId: siteId};
    webSocketConnection.sendObject("/av/editor/addService", payload);
    yield
}



export { editServiceData, editNetworkId, addService }
