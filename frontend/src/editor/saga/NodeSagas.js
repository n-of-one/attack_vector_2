import editorCanvas from "../component/EditorCanvas";
import webSocketConnection from "../../common/WebSocketConnection";
import {select} from 'redux-saga/effects'

const getSiteId = (state) => state.siteData.id;


function* dropNodeSaga(action) {
    const siteId = yield select(getSiteId);
    let x = action.x - action.dragAndDropState.dx;
    let y = action.y - action.dragAndDropState.dy;
    let nodeType = action.dragAndDropState.type.name.toUpperCase();
    let payload = {siteId: siteId, x, y, type: nodeType};
    let body = JSON.stringify(payload);
    webSocketConnection.send("/av/addNode", body);
    yield
}

function* serverNodeAddedSaga(action) {
    yield editorCanvas.addNodeAndSelect(action.data);
}


function* moveNodeSaga(action) {
    const siteId = yield select(getSiteId);
    let payload = {siteId: siteId, nodeId: action.id, x: action.x, y: action.y};
    let body = JSON.stringify(payload);
    webSocketConnection.send("/av/moveNode", body);
    yield
}

function* serverMoveNodeSaga(action) {
    yield editorCanvas.moveNode(action.data);
}

function* addConnectionSaga(action) {
    const siteId = yield select(getSiteId);
    let payload = {siteId: siteId, fromId: action.fromId, toId: action.toId};
    let body = JSON.stringify(payload);
    webSocketConnection.send("/av/addConnection", body);
    yield
}

function* serverAddConnectionSaga(action) {
    yield editorCanvas.addConnection(action.data);
}

function* deleteConnections(action) {
    const siteId = yield select(getSiteId);
    let payload = {siteId: siteId, nodeId: action.nodeId};
    let body = JSON.stringify(payload);
    webSocketConnection.send("/av/deleteConnections", body);
    yield
}

function* deleteNode(action) {
    const siteId = yield select(getSiteId);
    let payload = {siteId: siteId, nodeId: action.nodeId};
    let body = JSON.stringify(payload);
    webSocketConnection.send("/av/deleteNode", body);
    yield
}

function* snap(action) {
    const siteId = yield select(getSiteId);
    let payload = {siteId: siteId};
    let body = JSON.stringify(payload);
    webSocketConnection.send("/av/snap", body);
    yield
}


export {
    dropNodeSaga, serverNodeAddedSaga,
    moveNodeSaga, serverMoveNodeSaga,
    addConnectionSaga, serverAddConnectionSaga,
    deleteConnections, deleteNode, snap
};

