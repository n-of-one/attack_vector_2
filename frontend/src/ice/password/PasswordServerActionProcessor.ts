import {Store} from "redux";
import {webSocketConnection} from "../../common/server/WebSocketConnection";
import {passwordIceManager, } from "./container/PasswordIceManager";
import {IceAppEnter, IceAppStateUpdate, SERVER_ENTER_ICE_APP, SERVER_ICE_APP_UPDATE} from "../../app/iceApp/IceAppServerActionProcessor";

export const initPasswordIceServerActions = (store: Store) => {
    webSocketConnection.addAction(SERVER_ENTER_ICE_APP, (data: IceAppEnter) => {
        passwordIceManager.enter()
    })
    webSocketConnection.addAction(SERVER_ICE_APP_UPDATE, (data: IceAppStateUpdate) => {
        passwordIceManager.serverPasswordIceUpdate(data)
    })
}
