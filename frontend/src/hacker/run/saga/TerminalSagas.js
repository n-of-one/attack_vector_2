import {webSocketConnection} from "../../../common/WebSocketConnection";
import {select} from 'redux-saga/effects'



function* terminalSubmitCommandSaga(action) {
    const getRunId = (state) => state.run.scan.runId;
    const runId = yield select(getRunId);
    const payload = {runId: runId, command: action.command};
    let body = JSON.stringify(payload);
    webSocketConnection.send("/av/terminal/main", body);
    yield
}


export {terminalSubmitCommandSaga};