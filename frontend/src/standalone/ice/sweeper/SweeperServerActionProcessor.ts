import {Store} from "redux";
import {IceStrength} from "../../../common/model/IceStrength";
import {webSocketConnection} from "../../../common/server/WebSocketConnection";
import {sweeperIceManager} from "./SweeperIceManager";

export const SERVER_SWEEPER_ENTER = "SERVER_SWEEPER_ENTER"
export const SERVER_SWEEPER_MODIFY = "SERVER_SWEEPER_MODIFY"
export const SERVER_SWEEPER_SOLVED = "SERVER_SWEEPER_SOLVED"
export const SERVER_SWEEPER_BLOCK_USER = "SERVER_SWEEPER_BLOCK_USER"

export interface Point {
    x: number,
    y: number,
}

export interface SweeperEnterData{
    cells: string[],
    modifiers: string[],
    strength: IceStrength,
    hacked: boolean,
    blockedUserIds: string[]
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

interface SweeperBlockUserData {
    userId: string
}

export const initSweeperServerActions = (store: Store) => {
    webSocketConnection.addAction(SERVER_SWEEPER_ENTER, (data: SweeperEnterData) => {
        sweeperIceManager.enter(data)
    })

    webSocketConnection.addAction(SERVER_SWEEPER_MODIFY, (data: SweeperModifyData) => {
        sweeperIceManager.serverModified(data)
    })
    webSocketConnection.addAction(SERVER_SWEEPER_SOLVED, (data: any) => {
        sweeperIceManager.serverSolved()
    })
    webSocketConnection.addAction(SERVER_SWEEPER_BLOCK_USER, (data: SweeperBlockUserData) => {
        sweeperIceManager.blockUser(data.userId)
    })

}
