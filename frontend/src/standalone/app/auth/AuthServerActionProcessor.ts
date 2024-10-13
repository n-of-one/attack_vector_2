import {IceStrength} from "../../../common/model/IceStrength";
import {IceType} from "../../ice/common/IceModel";
import {webSocketConnection} from "../../../common/server/WebSocketConnection";
import {avEncodedUrl} from "../../../common/util/PathEncodeUtils";

export const SERVER_AUTH_ENTER = "SERVER_AUTH_ENTER"
export const SERVER_AUTH_UPDATE = "SERVER_AUTH_UPDATE"
export const SERVER_AUTH_PASSWORD_CORRECT = "SERVER_AUTH_PASSWORD_CORRECT"


export interface AuthEnter {
    type: IceType,
    strength: IceStrength,
    hint?: string,

    lockedUntil: string  // "2019-08-26T15:38:40.9179757+02:00"
    attempts: string[],
    showHint: boolean,
    hacked: boolean,
    quickPlaying: boolean,
}

export interface AuthStateUpdate {
    lockedUntil: string  // "2019-08-26T15:38:40.9179757+02:00"
    hacked: boolean,
    attempts: string[],
    showHint: boolean,
}

interface AuthSuccess {
    path: string | null
}

export const initAuthServerActions = () => {
    webSocketConnection.addAction(SERVER_AUTH_PASSWORD_CORRECT, (data: AuthSuccess) => {
        setTimeout(() => {
            if (data.path) {
                document.location.href = avEncodedUrl(data.path)
            }
        }, 2000)
    })

}
