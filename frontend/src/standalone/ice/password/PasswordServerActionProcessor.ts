import {webSocketConnection} from "../../../common/server/WebSocketConnection";
import {passwordIceManager} from "./PasswordIceManager";
import {AuthEnter, SERVER_AUTH_ENTER} from "../../app/auth/AuthServerActionProcessor";
import {SERVER_ICE_HACKED} from "../../../common/server/GenericServerActionProcessor";


export const initPasswordIceServerActions = () => {
    webSocketConnection.addAction(SERVER_AUTH_ENTER, (data: AuthEnter) => {
        passwordIceManager.enter(data)
    })
    webSocketConnection.addAction(SERVER_ICE_HACKED, () => {
        passwordIceManager.serverSentIceHacked()
    })
}
