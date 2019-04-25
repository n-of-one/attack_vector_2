import editorCanvas from "../component/canvas/EditorCanvas";

const createNodeSagas = (stompClient, siteId) => {


    function* requestSiteFullSaga(action) {
        stompClient.send("/av/siteFull", siteId);
        yield
    }

    function* serverSiteFullSaga(action) {
        yield editorCanvas.loadSite(action.data);
    }



    function* dropNodeSaga(action) {
        let x = action.x - action.dragAndDropState.dx;
        let y = action.y - action.dragAndDropState.dy;
        let nodeType = action.dragAndDropState.type.name.toUpperCase();

        console.log(new Date().getMilliseconds());

        let payload = { siteId: siteId, x, y, type: nodeType };
        let body = JSON.stringify(payload);
        stompClient.send("/av/addNode", body);
        yield
    }

    function* serverNodeAddedSaga(action) {
        yield editorCanvas.addNodeWithRender(action.data);
    }



    function* moveNodeSaga(action) {
        let payload = { siteId: siteId, nodeId: action.id, x: action.x, y: action.y };
        let body = JSON.stringify(payload);
        stompClient.send("/av/moveNode", body);
        yield
    }

    function* serverMoveNodeSaga(action) {
        yield editorCanvas.moveNode(action.data);
    }

    function* addConnectionSaga(action) {
        let payload = { siteId: siteId, fromId: action.fromId, toId: action.toId };
        let body = JSON.stringify(payload);
        stompClient.send("/av/addConnection", body);
        yield
    }

    function* serverAddConnectionSaga(action) {
        yield editorCanvas.addConnectionWithRender(action.data);
    }

    function* deleteConnections(action) {
        let payload = { siteId: siteId, nodeId: action.nodeId };
        let body = JSON.stringify(payload);
        stompClient.send("/av/deleteConnections", body);
        yield
    }

    function* deleteNode(action) {
        let payload = { siteId: siteId, nodeId: action.nodeId };
        let body = JSON.stringify(payload);
        stompClient.send("/av/deleteNode", body);
        yield
    }

    function* snap(action) {
        let payload = { siteId: siteId };
        let body = JSON.stringify(payload);
        stompClient.send("/av/snap", body);
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