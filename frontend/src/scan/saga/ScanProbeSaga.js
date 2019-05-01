import scanCanvas from "../component/canvas/ScanCanvas"


const createProbeSagas = (stompClient, scanId) => {

    function* serverProbeActionSaga(action) {
        scanCanvas.launchProbe(action.data);
        yield
    }

    function* probeArriveSaga(action) {
        const payload = {scanId: scanId, nodeId: action.nodeId, type: action.type};
        let body = JSON.stringify(payload);
        stompClient.send("/av/scan/probeArrive", body);
        yield
    }

    function* probeSuccessSaga(action) {
        const {nodeId, newStatus} = action.data;
        scanCanvas.probeSuccess(nodeId, newStatus);
        yield
    }

    return [
        serverProbeActionSaga, probeArriveSaga, probeSuccessSaga
    ];

};

export default createProbeSagas;
