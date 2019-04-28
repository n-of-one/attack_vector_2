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

    function* terminalSubmitSaga(action) {
        const payload = {scanId: scanId, command: action.command};
        let body = JSON.stringify(payload);
        stompClient.send("/av/scan/terminal", body);
        yield
    }

    return [
        requestScanFullSaga, serverScanFullSaga, terminalSubmitSaga
    ];

};

export default createScanSagas;
