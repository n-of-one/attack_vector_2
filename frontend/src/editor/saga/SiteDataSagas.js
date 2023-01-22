import editorCanvas from "../component/middle/middle/EditorCanvas";

function* serverSiteFullSaga(action) {
    yield editorCanvas.loadSite(action.data);
}

export {serverSiteFullSaga}