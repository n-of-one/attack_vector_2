import {Store} from "redux";
import {webSocketConnection} from "../../common/server/WebSocketConnection";
import {IceStrength} from "../../common/model/IceStrength";
import {passwordIceManager} from "../../ice/password/container/PasswordIceManager";
import {IceType} from "../../ice/IceModel";

export const SERVER_ENTER_ICE_APP = "SERVER_ENTER_ICE_APP"
export const SERVER_ICE_APP_UPDATE = "SERVER_ICE_APP_UPDATE";


export interface IceAppEnter {
    type: IceType,
    strength: IceStrength,
    hint?: string,

    lockedUntil: string  // "2019-08-26T15:38:40.9179757+02:00"
    hacked: boolean,
    attempts: string[],
    showHint: boolean,
}

export interface IceAppStateUpdate {
    lockedUntil: string  // "2019-08-26T15:38:40.9179757+02:00"
    hacked: boolean,
    attempts: string[],
    showHint: boolean,
}

export const initIceAppServerActions = (store: Store) => {
    // webSocketConnection.addAction(SERVER_ENTER_ICE_APP, (data: IceAppEnter) => {
    //     const iceId = store.getState().iceId
    // })
    webSocketConnection.addAction(SERVER_ICE_APP_UPDATE, (data: IceAppStateUpdate) => {
    })

}