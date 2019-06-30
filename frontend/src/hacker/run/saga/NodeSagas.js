import runCanvas from "../component/RunCanvas";

function* updateNodeStatusSaga(action) {
    const {nodeId, newStatus} = action.data;
    runCanvas.updateNodeStatus(nodeId, newStatus);
    yield
}

function* discoverNodesSaga(action) {
    const {nodeIds, connectionIds} = action.data;
    runCanvas.discoverNodes(nodeIds, connectionIds);
    yield
}

export {updateNodeStatusSaga, discoverNodesSaga}