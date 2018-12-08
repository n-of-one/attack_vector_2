import canvasMap from "../component/canvas/CanvasMap";

const createNodeSagas = (stompClient, siteId) => {


    function* requestSiteFullSaga(action) {
        stompClient.send("/app/siteFull", siteId);
        yield
    }

    function* serverSiteFullSaga(action) {
        yield canvasMap.loadSite(action.data);
    }



    function* dropNodeSaga(action) {
        let x = action.x - action.dragAndDropState.dx;
        let y = action.y - action.dragAndDropState.dy;
        let nodeType = action.dragAndDropState.type;

        console.log(new Date().getMilliseconds());

        let payload = { siteId: siteId, x, y, type: nodeType };
        let body = JSON.stringify(payload);
        stompClient.send("/app/addNode", body);
        yield
    }

    function* serverNodeAddedSaga(action) {
        yield canvasMap.addNodeWithRender(action.data);
    }



    function* moveNodeSaga(action) {
        let payload = { siteId: siteId, nodeId: action.id, x: action.x, y: action.y };
        let body = JSON.stringify(payload);
        stompClient.send("/app/moveNode", body);
        yield
    }

    function* serverMoveNodeSaga(action) {
        yield canvasMap.moveNode(action.data);
    }

    function* addConnectionSaga(action) {
        let payload = { siteId: siteId, from: action.from, to: action.to, connectionType: "DEFAULT" };
        let body = JSON.stringify(payload);
        stompClient.send("/app/addConnection", body);
        yield
    }

    function* serverAddConnectionSaga(action) {
        yield canvasMap.addConnectionWithRender(action.data);
    }

    function* deleteConnections(action) {
        let payload = { siteId: siteId, nodeId: action.nodeId };
        let body = JSON.stringify(payload);
        stompClient.send("/app/deleteConnections", body);
        yield
    }

    function* deleteNode(action) {
        let payload = { siteId: siteId, nodeId: action.nodeId };
        let body = JSON.stringify(payload);
        stompClient.send("/app/deleteNode", body);
        yield
    }

    function* snap(action) {
        let payload = { siteId: siteId };
        let body = JSON.stringify(payload);
        stompClient.send("/app/snap", body);
        yield
    }

    return [
        requestSiteFullSaga, serverSiteFullSaga,
        dropNodeSaga, serverNodeAddedSaga,
        moveNodeSaga, serverMoveNodeSaga,
        addConnectionSaga, serverAddConnectionSaga,
        deleteConnections, deleteNode, snap];
};


export default createNodeSagas