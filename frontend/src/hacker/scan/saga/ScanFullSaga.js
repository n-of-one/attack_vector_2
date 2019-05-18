import scanCanvas from "../component/ScanCanvas"


const createScanSagas = (stompClient, scanId) => {

    function* navigatePage(action) {
        window.location="/hacker";
        yield
    }

    function* serverNavigatePage(action) {
        window.location="/hacker";
        yield
    }

    function* enterScanSaga() {
        stompClient.send("/av/scan/enterScan", scanId);
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
        navigatePage, serverNavigatePage,
        enterScanSaga, serverScanFullSaga, terminalSubmitSaga, updateNodeStatusSaga, discoverNodesSaga
    ];

};

export default createScanSagas;
