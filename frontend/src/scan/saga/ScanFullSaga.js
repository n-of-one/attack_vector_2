import scanCanvas from "../component/canvas/ScanCanvas"


const createScanSagas = (stompClient, scanId) => {
    function* requestScanFullSaga() {
        stompClient.send("/av/scan/sendScan", scanId);
        yield
    }

    function* serverScanFullSaga(action) {
        scanCanvas.loadScan(action.data);
        yield
    }

    return [
        requestScanFullSaga, serverScanFullSaga
    ];

};

export default createScanSagas;
