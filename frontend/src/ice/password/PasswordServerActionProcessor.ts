import {Store} from "redux";
import {webSocketConnection} from "../../common/server/WebSocketConnection";
import {PasswordIceI, SERVER_ENTER_ICE_PASSWORD, SERVER_ICE_PASSWORD_UPDATE} from "./container/PasswordIceReducer";
import {passwordIceManager, PasswordIceStateUpdate} from "./container/PasswordIceManager";


export const initPasswordIceServerActions = (store: Store) => {
    webSocketConnection.addAction(SERVER_ENTER_ICE_PASSWORD, (data: PasswordIceI) => {
        passwordIceManager.enter()
    });
    webSocketConnection.addAction(SERVER_ICE_PASSWORD_UPDATE, (data: PasswordIceStateUpdate) => {
        passwordIceManager.serverPasswordIceUpdate(data)
    });

}