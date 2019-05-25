import editorCanvas from "../component/EditorCanvas";
import webSocketConnection from "../../common/WebSocketConnection";
import {select} from 'redux-saga/effects'

const getSiteId = (state) => state.siteData.id;

function* requestSiteFullSaga(action) {
    webSocketConnection.send("/av/siteFull", action.siteId);
    yield
}

function* serverSiteFullSaga(action) {
    yield editorCanvas.loadSite(action.data);
}


function* editSiteDataSaga(action) {
    const siteId = yield select(getSiteId);
    let payload = {siteId: siteId, field: action.field, value: action.value};
    let body = JSON.stringify(payload);
    webSocketConnection.send("/av/editSiteData", body);
    yield
}


export {requestSiteFullSaga, serverSiteFullSaga, editSiteDataSaga}