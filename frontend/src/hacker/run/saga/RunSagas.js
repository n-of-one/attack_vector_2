import scanCanvas from "../component/ScanCanvas";

function* enterRunSaga(action) {
    scanCanvas.enterRun(action.data.userId);
    yield
}

export {enterRunSaga}