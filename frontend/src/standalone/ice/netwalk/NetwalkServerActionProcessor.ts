import {webSocketConnection} from "../../../common/server/WebSocketConnection";
import {IceStrength} from "../../../common/model/IceStrength";
import {SERVER_NETWALK_ENTER, SERVER_NETWALK_NODE_ROTATED} from "./reducer/NetwalkStateReducer";
import {netwalkManager} from "./NetwalkManager";
import {SERVER_ICE_HACKED} from "../../../common/server/GenericServerActionProcessor";

export interface NetwalkCell {
    "type": CellType,
    "connected": boolean
}

export interface ServerEnterIceNetwalk {
    strength: IceStrength,
    uiState: string,
    cellGrid: NetwalkCell[][],
    wrapping: boolean,
    hacked: boolean,
    quickPlaying: boolean,
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


export const initNetwalkServerActions = () => {
    webSocketConnection.addAction(SERVER_NETWALK_ENTER, (data: ServerEnterIceNetwalk) => {
        netwalkManager.enter(data)
    })

    webSocketConnection.addAction(SERVER_NETWALK_NODE_ROTATED, (data: NetwalkRotateUpdate) => {
        netwalkManager.serverSentNodeRotated(data)
    })

    webSocketConnection.addAction(SERVER_NETWALK_NODE_ROTATED, (data: NetwalkRotateUpdate) => {
        netwalkManager.serverSentNodeRotated(data)
    })

    webSocketConnection.addAction(SERVER_ICE_HACKED, () => {
        netwalkManager.serverSentIceHacked()
    })
}
