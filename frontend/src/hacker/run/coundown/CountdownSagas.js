import runCanvas from "../component/RunCanvas";

export function* serverFlashPatrollerSaga(action) {
    yield runCanvas.flashSnifferPatroller(action.data)
}

export function* serverStartPatrollerSaga(action) {
    yield runCanvas.activateSnifferPatroller(action.data)
}

export function* serverPatrollerMoveSaga(action) {
    yield runCanvas.movePatroller(action.data)
}

export function* serverPatrollerLocksHackerSaga(action) {
    yield runCanvas.snifferCatchesHacker(action.data);
}
