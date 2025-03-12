import {Store} from "redux";
import {webSocketConnection} from "../../../common/server/WebSocketConnection";
import {IceStrength} from "../../../common/model/IceStrength";
import {tarManager} from "./component/TarManager";
import {SERVER_ICE_HACKED, SERVER_RESET_ICE} from "../../../common/server/GenericServerActionProcessor";

export const SERVER_TAR_ENTER = "SERVER_TAR_ENTER"
export const SERVER_TAR_UPDATE = "SERVER_TAR_UPDATE"

export interface TarEnter {
    totalUnits: number,
    unitsHacked: number,
    strength: IceStrength,
    unitsPerSecond: number,
    hacked: boolean,
    quickPlaying: boolean,
}

export interface TarStatusUpdate {
    unitsHacked: number,
    hacked: boolean,
}

export const initTarServerActions = (store: Store) => {
    webSocketConnection.addAction(SERVER_TAR_ENTER, (data: TarEnter) => {
        tarManager.enter(data)
    })

    webSocketConnection.addAction(SERVER_TAR_UPDATE, (data: TarStatusUpdate) => {
        tarManager.serverSentUpdate(data)
    })
    webSocketConnection.addAction(SERVER_ICE_HACKED, () => {
        tarManager.serverSentIceHacked()
    })
    webSocketConnection.addAction(SERVER_RESET_ICE, () => {
        tarManager.processTarIceReset()
    })
}
