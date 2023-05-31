import {Store} from "redux";
import {webSocketConnection} from "../../common/server/WebSocketConnection";
import {IceStrength} from "../../common/model/IceStrength";
import {slowIceManager} from "./component/SlowIceManager";

export const SERVER_ENTER_ICE_SLOW = "SERVER_ENTER_ICE_SLOW"
export const SERVER_SLOW_ICE_UPDATE = "SERVER_SLOW_ICE_UPDATE"

export interface SlowIceEnter {
    totalUnits: number,
    unitsHacked: number,
    strength: IceStrength,
    hacked: boolean,
    unitsPerSecond: number,
}

export interface SlowIceUpdate {
    unitsHacked: number,
    hacked: boolean
}

export interface SlowIceStatusUpdate {
    unitsHacked: number,
    hacked: boolean,
}

export const initSlowIceServerActions = (store: Store) => {
    webSocketConnection.addAction(SERVER_ENTER_ICE_SLOW, (data: SlowIceEnter) => {
        const iceId = store.getState().iceId
        slowIceManager.enter(iceId, data)
    })

    webSocketConnection.addAction(SERVER_SLOW_ICE_UPDATE, (data: SlowIceStatusUpdate) => {
        slowIceManager.serverSentUpdate(data)
    })

}