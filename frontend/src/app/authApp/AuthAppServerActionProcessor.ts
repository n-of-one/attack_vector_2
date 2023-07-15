import {IceStrength} from "../../common/model/IceStrength";
import {IceType} from "../../ice/IceModel";
import {webSocketConnection} from "../../common/server/WebSocketConnection";
import {avEncodedUrl} from "../../common/util/Util";

export const SERVER_AUTH_APP_ENTER = "SERVER_AUTH_APP_ENTER"
export const SERVER_AUTH_APP_UPDATE = "SERVER_AUTH_APP_UPDATE"
export const SERVER_AUTH_APP_PASSWORD_CORRECT = "SERVER_AUTH_APP_PASSWORD_CORRECT"


export interface AuthAppEnter {
    type: IceType,
    strength: IceStrength,
    hint?: string,

    lockedUntil: string  // "2019-08-26T15:38:40.9179757+02:00"
    hacked: boolean,
    attempts: string[],
    showHint: boolean,
}

export interface AuthAppStateUpdate {
    lockedUntil: string  // "2019-08-26T15:38:40.9179757+02:00"
    hacked: boolean,
    attempts: string[],
    showHint: boolean,
}

export const initAuthAppServerActions = (nextUrl: string) => {
    webSocketConnection.addAction(SERVER_AUTH_APP_PASSWORD_CORRECT, (_data: any) => {
        document.location.href = avEncodedUrl(nextUrl)
    })

}