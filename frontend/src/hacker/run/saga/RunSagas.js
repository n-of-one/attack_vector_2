import scanCanvas from "../component/ScanCanvas";

function* enterRunSaga(action) {
    scanCanvas.enterRun(action.data.userId, action.data.quick);
    yield
}

export {enterRunSaga}