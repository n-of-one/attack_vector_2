import {Store} from "redux";
import {webSocketConnection} from "../../../common/server/WebSocketConnection";
import {IceStrength} from "../../../common/model/IceStrength";
import {tarManager} from "./component/TarManager";

export const SERVER_TAR_ENTER = "SERVER_TAR_ENTER"
export const SERVER_TAR_UPDATE = "SERVER_TAR_UPDATE"

export interface TarEnter {
    totalUnits: number,
    unitsHacked: number,
    strength: IceStrength,
    hacked: boolean,
    unitsPerSecond: number,
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

}