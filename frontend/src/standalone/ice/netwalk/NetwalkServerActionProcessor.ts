import {Store} from "redux";
import {webSocketConnection} from "../../../common/server/WebSocketConnection";
import {IceStrength} from "../../../common/model/IceStrength";
import {SERVER_NETWALK_ENTER, SERVER_NETWALK_NODE_ROTATED} from "./reducer/NetwalkStateReducer";
import {netwalkManager} from "./NetwalkManager";

export interface NetwalkCell {
    "type": CellType,
    "connected": boolean
}

export interface ServerEnterIceNetwalk {
    strength: IceStrength,
    uiState: string,
    hacked: boolean,
    cellGrid: NetwalkCell[][],
    wrapping: boolean,
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
    webSocketConnection.addAction(SERVER_NETWALK_ENTER, (data: ServerEnterIceNetwalk) => {
        netwalkManager.enter(data)
    })

    webSocketConnection.addAction(SERVER_NETWALK_NODE_ROTATED, (data: NetwalkRotateUpdate) => {
        netwalkManager.serverSentNodeRotated(data)
    })

}