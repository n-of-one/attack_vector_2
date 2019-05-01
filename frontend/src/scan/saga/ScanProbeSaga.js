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

    return [
        serverProbeActionSaga, probeArriveSaga
    ];

};

export default createProbeSagas;
