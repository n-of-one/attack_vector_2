import scanCanvas from "../component/canvas/ScanCanvas"


const createProbeSagas = (stompClient, scanId) => {

    function* serverProbeActionSaga(action) {
        scanCanvas.launchProbe(action.data);
        yield
    }

    return [
        serverProbeActionSaga
    ];

};

export default createProbeSagas;
