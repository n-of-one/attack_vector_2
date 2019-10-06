import runCanvas from "../component/RunCanvas";

export function* serverFlashPatrollerSaga(action) {
    yield runCanvas.flashTracingPatroller(action.data)
}

export function* serverStartPatrollerSaga(action) {
    yield runCanvas.activateTracingPatroller(action.data)
}

export function* serverPatrollerMoveSaga(action) {
    yield runCanvas.movePatroller(action.data)
}

export function* serverPatrollerLocksHackerSaga(action) {
    yield runCanvas.patrollerLocksHacker(action.data);
}
