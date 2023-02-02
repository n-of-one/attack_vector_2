import {runCanvas} from "../component/RunCanvas";
import {delay} from "../../../common/saga/SagaUtil";

function* updateNodeStatusSaga(action) {
    runCanvas.updateNodeStatus(action.data);
    yield
}

function* discoverNodesSaga(action) {
    const {nodeIds, connectionIds} = action.data;
    runCanvas.discoverNodes(nodeIds, connectionIds);
    yield
}

export function* serverNodeHacked(action) {
    yield delay(action.data.delay);
    runCanvas.nodeHacked(action.data);
    yield
}

export {updateNodeStatusSaga, discoverNodesSaga}