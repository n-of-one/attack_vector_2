import scanCanvas from "../component/canvas/ScanCanvas"


const createScanSagas = (stompClient, scanId) => {
    function* requestScanFullSaga() {
        stompClient.send("/app/scan/sendScan", scanId);
        yield
    }

    function* serverScanFullSaga(action) {
        yield scanCanvas.loadSite(action.data);
    }

    return [
        requestScanFullSaga, serverScanFullSaga
    ];

};

export default createScanSagas;
