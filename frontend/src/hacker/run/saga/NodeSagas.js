import scanCanvas from "../component/ScanCanvas";

function* updateNodeStatusSaga(action) {
    const {nodeId, newStatus} = action.data;
    scanCanvas.updateNodeStatus(nodeId, newStatus);
    yield
}

function* discoverNodesSaga(action) {
    const {nodeIds, connectionIds} = action.data;
    scanCanvas.discoverNodes(nodeIds, connectionIds);
    yield
}

export {updateNodeStatusSaga, discoverNodesSaga}