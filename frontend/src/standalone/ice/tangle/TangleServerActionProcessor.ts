import {webSocketConnection} from "../../../common/server/WebSocketConnection";
import {SERVER_TANGLE_ENTER, SERVER_TANGLE_POINT_MOVED} from "./reducer/TangleIceReducer";
import {EnterTanglePuzzle, tangleIceManager, TanglePointMoved} from "./TangleIceManager";
import {SERVER_ICE_HACKED} from "../../../common/server/GenericServerActionProcessor";

export const initTangleIceServerActions = () => {
     webSocketConnection.addAction(SERVER_TANGLE_ENTER, (data: EnterTanglePuzzle) => {
         tangleIceManager.enter(data)
     })
     webSocketConnection.addAction(SERVER_TANGLE_POINT_MOVED, (data: TanglePointMoved) => {
         tangleIceManager.moved(data)
     })
    webSocketConnection.addAction(SERVER_ICE_HACKED, () => {
        tangleIceManager.serverSentIceHacked()
    })
 }
