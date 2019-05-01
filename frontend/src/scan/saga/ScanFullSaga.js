import scanCanvas from "../component/canvas/ScanCanvas"


const createScanSagas = (stompClient, scanId) => {

    function* navigatePage(action) {
        window.location="/hacker";
        yield
    }


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

    function* updateNodeStatusSaga(action) {
        const {nodeId, newStatus} = action.data;
        scanCanvas.updateNodeStatus(nodeId, newStatus);
        yield
    }

    function* discoverNodesSaga(action) {
        const {nodeIds, connectionIds} = action.data;
        scanCanvas.discoverNodes(nodeIds, connectionIds);
        yield
    }

    return [
        navigatePage,
        requestScanFullSaga, serverScanFullSaga, terminalSubmitSaga, updateNodeStatusSaga, discoverNodesSaga
    ];

};

export default createScanSagas;
