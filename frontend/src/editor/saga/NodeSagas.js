import editorCanvas from "../component/middle/middle/EditorCanvas";

function* serverNodeAddedSaga(action) {
    editorCanvas.addNode(action.data);
    editorCanvas.selectNode(action.data.id);
    yield
}

function* serverMoveNodeSaga(action) {
    yield editorCanvas.moveNode(action.data);
}

function* serverAddConnectionSaga(action) {
    yield editorCanvas.addConnection(action.data);
}


export {
    serverNodeAddedSaga,
    serverMoveNodeSaga,
    serverAddConnectionSaga
};
