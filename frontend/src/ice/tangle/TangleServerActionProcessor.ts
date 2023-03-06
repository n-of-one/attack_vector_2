import {Store} from "redux";
import {webSocketConnection} from "../../common/WebSocketConnection";
import {SERVER_ENTER_ICE_TANGLE, SERVER_TANGLE_POINT_MOVED} from "./TangleIceReducer";
import {tangleIceManager, TanglePointMoved, TanglePuzzle} from "./component/TangleIceManager";


export const initTangleIceServerActions = (store: Store) => {
    webSocketConnection.addAction(SERVER_ENTER_ICE_TANGLE, (data: TanglePuzzle) => {
        const iceId = store.getState().iceId
        tangleIceManager.enter(iceId, data)
    });
    webSocketConnection.addAction(SERVER_TANGLE_POINT_MOVED, (data: TanglePointMoved) => {
        tangleIceManager.moved(data)
    });

}