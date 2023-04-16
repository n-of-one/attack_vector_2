import {Store} from "redux";
import {webSocketConnection} from "../../common/WebSocketConnection";
import {IceStrength} from "../../common/model/IceStrength";
import {SERVER_ENTER_ICE_NETWALK, SERVER_NETWALK_NODE_ROTATED} from "./reducer/NetwalkStateReducer";
import {netwalkManager} from "./component/NetwalkManager";

export interface CellMinimal {
    "type": CellType,
    "connected": boolean
}

export interface ServerEnterIceNetwalk {
    strength: IceStrength,
    uiState: string,
    hacked: boolean,
    cellGrid: CellMinimal[][]
}

export enum CellType {
    N = "N",         // "╵"
    E = "E",         // "╶"
    S = "S",         // "╷"
    W = "W",         // "╴"
    NE = "NE",       // "└"
    SE = "SE",       // "┌"
    SW = "SW",       // "┐"
    NW = "NW",       // "┘"
    NS = "NS",       // "│"
    EW = "EW",       // "─"
    NES = "NES",     // "├"
    NEW = "NEW",     // "┴"
    NSW = "NSW",     // "┤"
    ESW = "ESW",     // "┬"
    NESW = "NESW",   // "┼"
    CENTER = "CENTER"// "*"
}

export interface Point {
    x: number,
    y: number,
}

export interface NetwalkRotateUpdate {
    iceId: string,
    x: number,
    y: number,
    connected: Point[],
    hacked: boolean
}


export const initNetwalkServerActions = (store: Store) => {
    webSocketConnection.addAction(SERVER_ENTER_ICE_NETWALK, (data: any) => {
        const iceId = store.getState().iceId
        netwalkManager.enter(iceId, data)
    })

    webSocketConnection.addAction(SERVER_NETWALK_NODE_ROTATED, (data: NetwalkRotateUpdate) => {
        netwalkManager.serverSentNodeRotated(data)
    })

}