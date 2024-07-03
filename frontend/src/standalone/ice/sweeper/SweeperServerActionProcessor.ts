import {Store} from "redux";
import {IceStrength} from "../../../common/model/IceStrength";
import {SweeperImageType} from "./SweeperModel";
import {webSocketConnection} from "../../../common/server/WebSocketConnection";
import {sweeperIceManager} from "./SweeperIceManager";

export const SERVER_SWEEPER_ENTER = "SERVER_SWEEPER_ENTER"
export const SERVER_SWEEPER_MODIFY = "SERVER_SWEEPER_MODIFY"


export interface Point {
    x: number,
    y: number,
}

export interface SweeperUpdate {
    iceId: string,
    x: number,
    y: number,
    tileStatus: SweeperImageType,
    hacked: boolean
}


export interface SweeperEnterData{
    cells: string[],
    modifiers: string[],
    strength: IceStrength,
    hacked: boolean,
}

export enum SweeperModifyAction {
    REVEAL = "REVEAL",
    FLAG = "FLAG",
    QUESTION_MARK = "QUESTION_MARK",
    CLEAR = "CLEAR",
    EXPLODE = "EXPLODE"
}

export interface SweeperModifyData  {
    cells: string[],
    action: SweeperModifyAction,
}

export const initSweeperServerActions = (store: Store) => {
    webSocketConnection.addAction(SERVER_SWEEPER_ENTER, (data: SweeperEnterData) => {
        sweeperIceManager.enter(data)
    })

    webSocketConnection.addAction(SERVER_SWEEPER_MODIFY, (data: SweeperModifyData) => {
        sweeperIceManager.serverModified(data)
    })

}
