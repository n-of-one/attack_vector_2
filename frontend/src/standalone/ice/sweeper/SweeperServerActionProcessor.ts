import {IceStrength} from "../../../common/model/IceStrength";
import {webSocketConnection} from "../../../common/server/WebSocketConnection";
import {sweeperIceManager} from "./SweeperIceManager";

export const SERVER_SWEEPER_ENTER = "SERVER_SWEEPER_ENTER"
export const SERVER_SWEEPER_MODIFY = "SERVER_SWEEPER_MODIFY"
export const SERVER_SWEEPER_SOLVED = "SERVER_SWEEPER_SOLVED"
export const SERVER_SWEEPER_BLOCK_USER = "SERVER_SWEEPER_BLOCK_USER"
export const SERVER_SWEEPER_UNBLOCK_USER = "SERVER_SWEEPER_UNBLOCK_USER"
export const SERVER_SWEEPER_RESET_START = "SERVER_SWEEPER_RESET_START"
export const SERVER_SWEEPER_RESET_STOP = "SERVER_SWEEPER_RESET_STOP"
export const SERVER_SWEEPER_RESET_COMPLETE = "SERVER_SWEEPER_RESET_COMPLETE"

export interface SweeperEnterData{
    cells: string[],
    modifiers: string[],
    strength: IceStrength,
    blockedUserIds: string[],
    minesLeft: number,
    hacked: boolean,
    quickPlaying: boolean,

}

export enum SweeperModifyAction {
    REVEAL = "REVEAL",
    FLAG = "FLAG",
    CLEAR = "CLEAR",
    EXPLODE = "EXPLODE"
}

export interface SweeperModifyData  {
    cells: string[],
    action: SweeperModifyAction,
}

interface SweeperBlockUserData {
    userId: string,
    userName: string,
}
interface SweeperResetData {
    userName: string,
}

interface SweeperResetCompleteData {
    userName: string,
    newIceId: string,
}

export const initSweeperServerActions = () => {
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
        sweeperIceManager.blockUser(data.userId, data.userName)
    })
    webSocketConnection.addAction(SERVER_SWEEPER_UNBLOCK_USER, (data: SweeperBlockUserData) => {
        sweeperIceManager.unblockUser(data.userId, data.userName)
    })
    webSocketConnection.addAction(SERVER_SWEEPER_RESET_START, (data: SweeperResetData) => {
        sweeperIceManager.startReset(data.userName)
    })
    webSocketConnection.addAction(SERVER_SWEEPER_RESET_STOP, (data: SweeperResetData) => {
        sweeperIceManager.stopReset(data.userName)
    })
    webSocketConnection.addAction(SERVER_SWEEPER_RESET_COMPLETE, (data: SweeperResetCompleteData) => {
        sweeperIceManager.completeReset(data.userName, data.newIceId)
    })


}
