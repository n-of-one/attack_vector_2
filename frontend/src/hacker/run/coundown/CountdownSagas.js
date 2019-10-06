import runCanvas from "../component/RunCanvas";

export function* serverFlashPatrollerSaga(action) {
    yield runCanvas.flashPatroller(action.data)
}

export function* serverStartPatrollerSaga(action) {
    yield runCanvas.activateSniffer(action.data)
}

export function* serverPatrollerMoveSaga(action) {
    yield runCanvas.movePatroller(action.data)
}

export function* serverPatrollerLocksHackerSaga(action) {
    yield runCanvas.leashLocksHacker(action.data);
}
