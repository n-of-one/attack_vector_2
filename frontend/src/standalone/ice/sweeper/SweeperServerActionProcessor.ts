import {Store} from "redux";
import {IceStrength} from "../../../common/model/IceStrength";
import {SweeperGameState, SweeperImage} from "./SweeperModel";
import {webSocketConnection} from "../../../common/server/WebSocketConnection";
import {sweeperIceManager} from "./SweeperIceManager";

export const SERVER_SWEEPER_ENTER = "SERVER_SWEEPER_ENTER"



export interface Point {
    x: number,
    y: number,
}

export interface SweeperUpdate {
    iceId: string,
    x: number,
    y: number,
    tileStatus: SweeperImage,
    hacked: boolean
}


export interface SweeperEnterData{
    grid: string[],
    strength: IceStrength,
    hacked: boolean,
}

export const initSweeperServerActions = (store: Store) => {
    webSocketConnection.addAction(SERVER_SWEEPER_ENTER, (data: SweeperEnterData) => {
        sweeperIceManager.enter(data)
    })
    //
    // webSocketConnection.addAction(SERVER_NETWALK_NODE_ROTATED, (data: NetwalkRotateUpdate) => {
    //     netwalkManager.serverSentNodeRotated(data)
    // })

}