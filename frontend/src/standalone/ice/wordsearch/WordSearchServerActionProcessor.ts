import {webSocketConnection} from "../../../common/server/WebSocketConnection";
import {SERVER_WORD_SEARCH_ENTER} from "./reducer/WordSearchPuzzleReducer";
import {wordSearchManager} from "./WordSearchManager";
import {delay} from "../../../common/util/Util";
import {IceStrength} from "../../../common/model/IceStrength";
import {SERVER_ICE_HACKED} from "../../../common/server/GenericServerActionProcessor";

export const SERVER_WORD_SEARCH_UPDATED = "SERVER_WORD_SEARCH_UPDATED"

export interface ServerEnterIceWordSearch {
    layerId: string,
    strength: IceStrength,
    letterGrid: string[][],
    words: string[],
    correctPositions: [],
    solutions: string[][]
    wordIndex: 0,
    hacked: false,
    quickPlaying: boolean
}


export interface UpdateAction {
    iceId: string,
    lettersCorrect: string[],
    wordIndex: number,
    hacked: boolean
}

export const initWordSearchServerActions = () => {
    webSocketConnection.addAction(SERVER_WORD_SEARCH_ENTER, (data: ServerEnterIceWordSearch) => {
        wordSearchManager.enter(data)
    })
    webSocketConnection.addAction(SERVER_WORD_SEARCH_UPDATED, (data: UpdateAction) => {
        delay(() => {
            wordSearchManager.update(data)
        })
    })
    webSocketConnection.addAction(SERVER_ICE_HACKED, () => {
        wordSearchManager.serverSentIceHacked()
    })
}
