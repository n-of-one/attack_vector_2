import scanCanvas from "../component/canvas/ScanCanvas"


const createProbeSagas = (stompClient, scanId) => {

    function* serverProbeLaunchSaga(action) {
        scanCanvas.launchProbe(action.data);
        yield
    }

    function* probeArriveSaga(action) {
        const payload = {scanId: scanId, nodeId: action.nodeId, action: action.action};
        let body = JSON.stringify(payload);
        stompClient.send("/av/scan/probeArrive", body);
        yield
    }

    function* autoScanSaga() {
        stompClient.send("/av/scan/autoScan", scanId);
        yield
    }

    return [
        serverProbeLaunchSaga, probeArriveSaga, autoScanSaga
    ];

};

export default createProbeSagas;
