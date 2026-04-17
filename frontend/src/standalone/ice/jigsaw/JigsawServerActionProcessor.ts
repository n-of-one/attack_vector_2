import {IceStrength} from "../../../common/model/IceStrength";
import {webSocketConnection} from "../../../common/server/WebSocketConnection";
import {jigsawIceManager} from "./JigsawIceManager";
import {SERVER_ICE_HACKED, SERVER_RESET_ICE} from "../../../common/server/GenericServerActionProcessor";
import {Group, PieceConfig} from "./component/JigsawShapes";

export const SERVER_JIGSAW_ENTER = "SERVER_JIGSAW_ENTER"
export const SERVER_JIGSAW_MOVED = "SERVER_JIGSAW_MOVED"
export const SERVER_JIGSAW_ROTATE = "SERVER_JIGSAW_ROTATE"
export const SERVER_JIGSAW_SNAP = "SERVER_JIGSAW_SNAP"

export interface JigsawEnterData {
    hacked: boolean,
    strength: IceStrength,
    imageSource: string,
    columns: number,
    rows: number,
    pieces: PieceConfig[],
    groups: Group[],
}

export interface JigsawMovedPayload {
    groupId: string,
    x: number,
    y: number,
}

export interface JigsawRotatePayload {
    groupId: string,
    rotation: number,
}

export interface JigsawSnapPayload {
    survivingGroupId: string,
    absorbedGroupIds: string[],
    pieces: Array<{ column: number, row: number }>,
    rotation: number,
    x: number,
    y: number,
}

export const initJigsawServerActions = () => {
    webSocketConnection.addAction(SERVER_JIGSAW_ENTER, (data: JigsawEnterData) => {
        jigsawIceManager.enter(data)
    })
    webSocketConnection.addAction(SERVER_JIGSAW_MOVED, (data: JigsawMovedPayload) => {
        jigsawIceManager.onGroupMoved(data)
    })
    webSocketConnection.addAction(SERVER_JIGSAW_ROTATE, (data: JigsawRotatePayload) => {
        jigsawIceManager.onGroupRotated(data)
    })
    webSocketConnection.addAction(SERVER_JIGSAW_SNAP, (data: JigsawSnapPayload) => {
        jigsawIceManager.onSnap(data)
    })
    webSocketConnection.addAction(SERVER_ICE_HACKED, () => {
        jigsawIceManager.iceHacked()
    })
    webSocketConnection.addAction(SERVER_RESET_ICE, () => {
        jigsawIceManager.processIceReset()
    })
}
