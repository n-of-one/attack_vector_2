import {runCanvas} from "../component/RunCanvas";

export function* startAttackSaga(action) {
    runCanvas.startAttack(action.data.userId, action.data.quick);
    yield
}

export function* moveStartSaga(action) {
    runCanvas.moveStart(action.data);
    yield
}

export function* serverMoveArriveSaga(action) {
    runCanvas.moveArrive(action.data);
    yield
}




export function* serverHackerProbeLayersSaga(action) {
    runCanvas.hackerProbeLayersSaga(action.data);
    yield
}


export function* serverHackerProbeConnectionsSaga(action) {
    runCanvas.hackerProbeConnections(action.data);
    yield
}

