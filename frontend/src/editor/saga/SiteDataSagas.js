const createSiteDataSagas = (stompClient, siteId) => {

    function* editSiteDataSaga(action) {
        let payload = { siteId: siteId, field: action.field, value: action.value };
        let body = JSON.stringify(payload);
        stompClient.send("/app/editSiteData", body);
        yield
    }

    return [ editSiteDataSaga ];
};


export default createSiteDataSagas